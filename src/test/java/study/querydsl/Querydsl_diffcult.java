package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class Querydsl_diffcult {
    @PersistenceContext
    EntityManager em;

    JPAQueryFactory query;

    @PostConstruct
    public  void init(){
        this.query = new JPAQueryFactory(em);
    }

    @BeforeEach
    public void initdata(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for(int i=0;i<5;i++){

            em.persist(new Member("member"+i, 10+i, teamA));
        }
        for(int i=0;i<5;i++){

            em.persist(new Member("member"+i+5, 10+i+5, teamB));
        }
        em.flush();
        em.clear();
    }


    @Test
    public void 튜플예시1(){
        // 지정되지않는  데이터들으 부를때 좋다.
        List<Tuple> fetch = query.select(member.username, member.age)
                .from(member)
                .fetch();

        for(Tuple t: fetch){
            String username = t.get(member.username);
            Integer age= t.get(member.age);
            System.out.println("username="+ username);
            System.out.println("age="+age);
        }
    }


    @Test
    public void JPQLDTO_예시(){
        List<MemberDto> resultList = em.createQuery("select new study.querydsl.dto.MemberDto(m.username,m.age) from Member m", MemberDto.class).getResultList();

        for(MemberDto d : resultList){
            System.out.println(d);
        }

    }

    // 비어있는 생성자가 필요함.
    @Test
    public void 쿼리dsl_dto방식(){
        List<MemberDto> fetch = query.select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for(MemberDto d : fetch){
            System.out.println(d);
        }
    }

    // 생성자 방식이 필요없는
    @Test
    public void 쿼리dsl_dto방식_필드방식(){
        List<MemberDto> fetch = query.select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for(MemberDto d : fetch){
            System.out.println(d);
        }
    }
    // 생성자 방식이 필요없는
    @Test
    public void 쿼리dsl_dto방식_생성자(){

        QMember membersub=new QMember("memberSub");

        List<UserDto> fetch = query
                        .select(Projections.constructor(UserDto.class,
                                member.username.as("name"),
                                ExpressionUtils.as(
                                        JPAExpressions
                                                .select(membersub.age.max())
                                                .from(membersub),"age")
                        ))
                        .from(member)
                        .fetch();

        for(UserDto d : fetch){
            System.out.println(d);
        }
    }
}

package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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
import study.querydsl.dto.QMemberDto;
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
//    @Test
//    public void 쿼리dsl_dto방식(){
//        List<MemberDto> fetch = query.select(Projections.bean(MemberDto.class, member.username, member.age))
//                .from(member)
//                .fetch();
//
//        for(MemberDto d : fetch){
//            System.out.println(d);
//        }
//    }

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


    @Test
    public void findDtoByQueryProjection(){
        List<MemberDto> fetch = query
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();


        fetch.stream().forEach(e-> System.out.println(e));
    }


    // 동적 쿼리 booleanBuilder
    @Test
    public  void  booleanBuilder(){
        String usernameParam ="member1";
        Integer ageParam= 10;

       List<Member> list= searchMember1(usernameParam,ageParam);
       list.stream().forEach(e -> System.out.println(e));
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {

        BooleanBuilder builder= new BooleanBuilder();
        if(usernameParam !=null){
            builder.and(member.username.eq(usernameParam));
        }
        if(ageParam !=null){
            builder.and(member.age.eq(ageParam));
        }

        return query
                .selectFrom(member)
                .where(builder)
                .fetch();
    }


    //다중 where 붙어 사용하기.
    @Test
    public void 다중_where(){
        String usernameParam ="member1";
        Integer ageParam= null;

        List<Member> list= searchMember2(usernameParam,ageParam);
        list.stream().forEach(e -> System.out.println(e));


    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return query
                .selectFrom(member)
                .where(usernameEq(usernameParam), ageEq(ageParam))
                .fetch();
    }

    // 다른 쿼리에서도 재활용성이 높아진다.
    private List<Member> searchMember3(String usernameParam, Integer ageParam) {
        return query
                .selectFrom(member)
                .where(allEq(usernameParam,ageParam))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameParam) {
        if( usernameParam!=null){
            return member.username.eq(usernameParam);
        }else{
            return null;
        }
    }

    private BooleanExpression  ageEq(Integer ageParam) {
        if(ageParam !=null){
            return member.age.eq(ageParam);
        }else{
            return null;
        }
    }


    private BooleanExpression allEq(String username, Integer age){
        return usernameEq(username).and(ageEq(age));
    }
    // 벌크 연산은
    // 영속성 컨텍스트가 달라진다. 플러쉬 클리어가 필수다.
    @Test
    public void  bulkUpdate(){
        Long count=  query.update(member)
                .set(member.username,"비회원")
                .where(member.age.lt(28))
                .execute();
        em.flush();
        em.clear();


        System.out.println(count);
    }

    @Test
    public void bulkAdd(){
        query
                .update(member)
           //     .set(member.age,member.age.add(1))  // 음수 처리하고 싶을땐 -1
           //     .set(member.age,member.age.multiply(3))  // 곱
                .set(member.age,member.age.divide(3))  // 나눗셈
                .execute();
    }

    @Test
    public void bulkdlelete(){
        query.delete(member)
                .where(member.username.eq("삭제 대상"))
                .execute();

    }


    @Test
    public  void  sqlFunction(){
        List<String> fetch = query.select(
                        Expressions.stringTemplate(
                                "function ('replace', {0} , {1} , {2})",
                                member.username, "member", "m")
                ).from(member)
                .fetch();
        fetch.stream().forEach(e -> System.out.println(e.toString()));

    }


    @Test
    public  void  sqlFunction2(){
        List<String> fetch = query.select(member.username)
                .from(member)
                .where(
          //              member.username.eq(Expressions.stringTemplate("function('lower',{0})", member.username))
                member.username.eq(member.username.lower())
                ).fetch();
        fetch.stream().forEach(e -> System.out.println(e.toString()));
    }
}

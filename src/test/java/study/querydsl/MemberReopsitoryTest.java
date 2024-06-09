package study.querydsl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class MemberReopsitoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;


    @Test
    public  void  basicTest() {
        Member m = new Member("member1", 10);
        memberRepository.save(m);


        memberRepository.findByUsername("member1");
    }


    @Test
    public void searchtest(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1= new Member("member1",10,teamA);
        Member member2= new Member("member2",20,teamA);
        Member member3= new Member("member3",30,teamB);
        Member member4= new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        MemberSearchCondition con=  new MemberSearchCondition();
        con.setAgeGoe(28);
        con.setAgeLoe(40);
        con.setTeamName("teamB");

        List<MemberTeamDto> memberTeamDtos = memberRepository.search(con);

        memberTeamDtos.stream().forEach(e -> System.out.println(e));
    }


    @Test
    public void searchtest2(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1= new Member("member1",10,teamA);
        Member member2= new Member("member2",20,teamA);
        Member member3= new Member("member3",30,teamB);
        Member member4= new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        MemberSearchCondition con=  new MemberSearchCondition();
//        con.setAgeGoe(28);
//        con.setAgeLoe(40);
//        con.setTeamName("teamB");
        PageRequest of = PageRequest.of(0, 3);

        Page<MemberTeamDto> memberTeamDtos = memberRepository.searchPageSimple(con, of);

        memberTeamDtos.getContent().stream().forEach(e -> System.out.println(e));

        System.out.println(memberTeamDtos.getSize());
    }


    @Test
    public void querydslQuerydslPredicateExecutor(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1= new Member("member1",10,teamA);
        Member member2= new Member("member2",20,teamA);
        Member member3= new Member("member3",30,teamB);
        Member member4= new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        Iterable<Member> result = memberRepository.findAll(member.age.between(20, 40).and(member.username.eq("member2")));
        for(Member m : result){
            System.out.println(m);
        }

    }
}

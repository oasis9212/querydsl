package study.querydsl;


import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;


    @Test
    public  void  basicTest() {
        Member m = new Member("member1", 10);
        memberJpaRepository.save(m);


        memberJpaRepository.findAll_queryDsl();
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
        con.setAgeGoe(35);
        con.setAgeLoe(40);
        con.setTeamName("teamB");

        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.search(con);

        memberTeamDtos.stream().forEach(e -> System.out.println(e));
    }


}

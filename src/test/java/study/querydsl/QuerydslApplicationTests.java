package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	//@Autowired
	@PersistenceContext
	EntityManager em;

	JPAQueryFactory query;

	@PostConstruct
	public  void init(){
		this.query = new JPAQueryFactory(em);
	}

	@BeforeEach
	public void initdata(){
		for(int i=0;i<10;i++){

			em.persist(new Member("member"+i, 10+i));
		}
		em.flush();
		em.clear();
	}

//	@Test
//	void contextLoads() {
//		Hello hello= new Hello();
//		em.persist(hello);
//
//		JPAQueryFactory query= new JPAQueryFactory(em);
//		QHello qh= new QHello("h");
//
//		Hello res= query.selectFrom(qh)
//				.fetchOne();
//
//		assertThat(res).isEqualTo(hello);
//		System.out.println(res.getId()+"asd");
//	}


	@Test
	public void testEntity() {
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);
		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
//초기화
		em.flush();
		em.clear();
		List<Member> members = em.createQuery("select m from Member m",
						Member.class)
				.getResultList();
		for (Member member : members) {
			System.out.println("member=" + member);
			System.out.println("-> member.team=" + member.getTeam());
		}
	}


	@Test
	public  void startquerydsl(){
		Member member1 = new Member("member1", 10);
		em.persist(member1);
		em.flush();
		em.clear();
		Member find= query
				.select(member)
				.from(member)
				.where(member.username.eq("member1"))
				.fetchOne();

		assertThat(find.getUsername()).isEqualTo("member1");
	}

	@Test
	public void search(){
		Member member1 = new Member("member1", 10);
		em.persist(member1);
		em.flush();
		em.clear();
		Member member11 = query.selectFrom(member)
				.where(member.username.eq("member1")
						.and(member.age.between(10,30)))
				.fetchOne();
		assertThat(member11.getUsername()).isEqualTo("member1");
	}


	@Test
	public void resutFetchtest(){
//		List<Member> fetch = query
//				.selectFrom(member)
//				.fetch();
//
//		Member fetchOne=  query
//				.selectFrom(member)
//				.fetchOne();
//
//		Member memberFirst = query
//				.selectFrom(member)
//				.fetchFirst();

//		QueryResults<Member> res= query.selectFrom(member)
//				.fetchResults();
//
//		res.getTotal();
//		List<Member> conten = res.getResults();

		long total = query
				.selectFrom(member)
				.fetchCount();
	}
}

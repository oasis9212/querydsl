package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
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
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

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

	/*
		정렬 순서
		1. 회원 나이 내림차순(desc)
		2. 회원 이름 오름차순(asc)
		 	회원 이름이 없다면 마지막에 출력
	 */
	@Test
	public void  sort(){
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> fetch = query
				.selectFrom(member)
				.where(member.age.eq(100))
				.orderBy(member.age.desc(), member.username.asc().nullsLast())
				.fetch();
		Member member5= fetch.get(0);
		Member member6= fetch.get(1);
		Member membernull= fetch.get(2);

		System.out.println(member5.getUsername());
		System.out.println(member6.getUsername());
		System.out.println(membernull.getUsername());


	}

	@Test
	public void  pageing1(){
		QueryResults<Member> memberQueryResults = query
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)  // 스킵
				.limit(2)
				.fetchResults();

		System.out.println(memberQueryResults.getTotal());
		System.out.println(memberQueryResults.getLimit());
		System.out.println(memberQueryResults.getOffset());
		System.out.println(memberQueryResults.getResults().size());

	}


	@Test
	public void 집합(){
		List<Tuple> result = query
				.select(
						member.count(),
						member.age.sum(),
						member.age.avg(),
						member.age.max(),
						member.age.min()
				)
				.from(member)
				.fetch();
		// 데이터의 여러 타입이 들어올땐 튜플로
		Tuple tuple = result.get(0);
		System.out.println(tuple.get(member.count()));
		System.out.println(tuple.get(member.age.sum()));
		System.out.println(tuple.get(member.age.avg()));
		System.out.println(tuple.get(member.age.max()));
		System.out.println(tuple.get(member.age.min()));
	}


	// 팀의 이름과 각팀의 나이 평균을 구하자.
	@Test
	public void groupBy(){
		List<Tuple> fetch = query
				.select(team.name, member.age.avg())
				.from(member)
				.join(member.team, team)
				.groupBy(team.name)
				.fetch();

		Tuple teamA = fetch.get(0);
		Tuple teamB = fetch.get(1);

		System.out.println(teamA.get(team.name));
		System.out.println(teamA.get(member.age.avg()));

		System.out.println(teamB.get(team.name));
		System.out.println(teamB.get(member.age.avg()));
	}
	// 팀 A에 소속된 모든 회원
	@Test
	public void join(){
		List<Member> teamA = query
				.selectFrom(member)
				.join(member.team, team)
				.where(team.name.eq("teamA"))
				.fetch();

		teamA.stream().forEach(e -> System.out.println(e.toString()));
	}
	// 세타 조인
	// 회원의 이름이 팀이름과 같은 회원 조회

	@Test
	public  void  theta_join(){
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));


		List<Member> fetch = query
				.select(member)
				.from(member, team)
				.where(member.username.eq(team.name))
				.fetch();

		assertThat(fetch)
				.extracting("username")
				.containsExactly("teamA","teamB");

	}

}

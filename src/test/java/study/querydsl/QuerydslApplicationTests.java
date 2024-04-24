package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import  org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	//@Autowired
	@PersistenceContext
	EntityManager em;


	@Test
	void contextLoads() {
		Hello hello= new Hello();
		em.persist(hello);

		JPAQueryFactory query= new JPAQueryFactory(em);
		QHello qh= new QHello("h");

		Hello res= query.selectFrom(qh)
				.fetchOne();

		assertThat(res).isEqualTo(hello);
		System.out.println(res.getId()+"asd");
	}

}

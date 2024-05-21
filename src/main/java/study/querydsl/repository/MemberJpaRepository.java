package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory factory;



    public  void  save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member findmember= em.find(Member.class, id);
        return Optional.ofNullable(findmember);
    }
    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findAll_queryDsl(){
        return factory.selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsernaem(String username){
        return em.createQuery("select m from Member m where m.username = :username",Member.class)
                .setParameter("username",username)
                .getResultList();
    }

    public List<Member> findByUsernaem_querydsl(String username){
        return factory.selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }
}

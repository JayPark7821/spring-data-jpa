package study.datajpa.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void testMember() {
        System.out.println("memberRepository.getClass() = " + memberRepository.getClass());
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();// orElse

        assertThat(findMember.getId()).isEqualTo(savedMember.getId());
        assertThat(findMember.getUsername()).isEqualTo(savedMember.getUsername());
        assertThat(findMember).isEqualTo(savedMember);

    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        Long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        Long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);

    }

    @Test
    void findByUsernameAndAgeGreaterThen() {
        Member m1 = new Member("aaaa", 10);
        Member m2 = new Member("aaaa", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("aaaa", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("aaaa");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    void testNamedQuery() {
        Member m1 = new Member("aaaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("aaaa");
        Member member = result.get(0);
        assertThat(member).isEqualTo(m1);
    }

    @Test
    void testQuery() {
        Member m1 = new Member("aaaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("aaaa", 10);
        Member member = result.get(0);
        assertThat(member).isEqualTo(m1);
    }

    @Test
    void findUsernameList() {
        Member m1 = new Member("aaaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findusernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void findMemberDto() {

        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("aaaa", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    void findByNames() {
        Member m1 = new Member("aaaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("aaaa", "bbb"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void returnType() {
        Member m1 = new Member("aaaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> aaaa = memberRepository.findListByUsername("aaaa");
        Optional<Member> aaaa1 = memberRepository.findOptionalByUsername("aaaa");
        Member aaaa2 = memberRepository.findMemberByUsername("aaaa");

    }

    @Test
    void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findPageByAge(10, pageRequest);

        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        Slice<Member> slice = memberRepository.findSliceByAge(10, pageRequest);

        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(totalElements).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }

    @Test
    void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 20));
        memberRepository.save(new Member("member3", 30));
        memberRepository.save(new Member("member4", 40));
        memberRepository.save(new Member("member5", 50));
        int resultCount = memberRepository.bulkAgePlus(20);
        em.flush();
        em.clear();
        assertThat(resultCount).isEqualTo(4);
    }

    @Test
    void findMemberLazy() {
        //given
        // member1 -> teamA
        // member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    void queryHint() {
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");

//        findMember.setUsername("member2");
        em.flush();

    }

    @Test
    void lock() {
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        List<Member> result = memberRepository.findLockByUsername("member1");

    }

    @Test
    void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }


    @Test
    void queryByExample() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //probe
        Member member = new Member("m1");
        Team team = new Team("teamA");
        member.setTeam(team);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("age");
        Example<Member> example = Example.of(member,matcher);

        List<Member> result = memberRepository.findAll(example);

        assertThat(result.get(0).getUsername()).isEqualTo("m1");

    }

    @Test
    void nativeQuery() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection user = " + memberProjection.getUsername());
            System.out.println("memberProjection team = " + memberProjection.getTeamName());
        }
        System.out.println("result = " + result);
    }

    @Test
    void projections() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");
        for (UsernameOnly usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly);
        }

    }

    @Test
    void projectionsDto() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        List<UsernameOnlyDto> result = memberRepository.findProjectionsByUsernameDto("m1");
        List<NestedClosedProjections> result2 = memberRepository.findProjectionsByT("m1", NestedClosedProjections.class);
        for (UsernameOnlyDto usernameOnlyDto : result) {

            System.out.println("usernameOnlyDto = " + usernameOnlyDto);
        }

    }

}


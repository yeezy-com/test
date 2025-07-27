package demo.demo.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface CourseRepository extends Repository<Course, Long> {

    List<Course> saveAll(Iterable<Course> courses);

    List<Course> findAll();

    Optional<Course> findById(Long id);

    default List<Course> findAllHasDistanceWithin(Coordinate target, Meter meter) {
        return findAll().stream()
                .filter(c -> c.distanceFrom(target).isWithin(meter))
                .toList();
    }
}

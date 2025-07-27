package demo.demo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class CourseTest {

    @Nested
    class 생성_테스트 {

        private static final String LENGTH_31_NAME = "1234567890123456789012345678901";

        @Test
        void 코스를_생성한다() {
            assertThatCode(() -> new Course("코스이름", getNormalCoordinates()))
                    .doesNotThrowAnyException();
        }

        @Test
        void 앞_뒤_공백을_제거하여_생성한다() {
            var course = new Course(" 코스이름   ", getNormalCoordinates());
            assertThat(course.name()).isEqualTo("코스이름");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                LENGTH_31_NAME,
                "짧"
        })
        void 잘못된_길이의_이름으로_코스를_생성하면_예외가_발생한다(String name) {
            assertThatThrownBy(() -> new Course(name, getNormalCoordinates()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "코스 이름",
                "코스  이름",
                "코스   이름",
                "코스    이름",
        })
        void 이름의_연속공백을_한_칸으로_변환하여_코스를_생성한다(String name) {
            var course = new Course(name, getNormalCoordinates());
            assertThat(course.name()).isEqualTo("코스 이름");
        }

        @Test
        void 코스의_좌표의_개수가_2보다_적으면_예외가_발생한다() {
            assertThatThrownBy(() -> new Course("코스이름", List.of(new Coordinate(1d, 1d))))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 반시계방향이면_그대로_설정된다() {
            var counter = new Course("코스이름", List.of(
                    new Coordinate(0d, 0d),
                    new Coordinate(5d, 5d),
                    new Coordinate(0d, 10d),
                    new Coordinate(5d, -5d),
                    new Coordinate(0d, 0d)
            ));

            assertThat(counter.coordinates()).containsExactly(
                    new Coordinate(0d, 0d),
                    new Coordinate(5d, 5d),
                    new Coordinate(0d, 10d),
                    new Coordinate(5d, -5d),
                    new Coordinate(0d, 0d)
            );
        }

        @Test
        void 시계방향이면_반대로_설정된다() {
            var clockwiseCourse = new Course("코스이름", List.of(
                    new Coordinate(0d, 0d),
                    new Coordinate(5d, -5d),
                    new Coordinate(0d, 10d),
                    new Coordinate(5d, 5d),
                    new Coordinate(0d, 0d)
            ));

            assertThat(clockwiseCourse.coordinates()).containsExactly(
                    new Coordinate(0d, 0d),
                    new Coordinate(5d, 5d),
                    new Coordinate(0d, 10d),
                    new Coordinate(5d, -5d),
                    new Coordinate(0d, 0d)
            );
        }

        private static List<Coordinate> getNormalCoordinates() {
            return List.of(new Coordinate(1d, 1d), new Coordinate(1d, 1d));
        }
    }

    @Test
    void 코스의_총_거리를_계산할_수_있다() {
        var course = new Course("한강뛰어보자", List.of(
                new Coordinate(37.518400, 126.995600),
                new Coordinate(37.518000, 126.996500),
                new Coordinate(37.517500, 126.998000),
                new Coordinate(37.517000, 127.000000),
                new Coordinate(37.516500, 127.002000),
                new Coordinate(37.516000, 127.004500),
                new Coordinate(37.515500, 127.007000),
                new Coordinate(37.515000, 127.009500),
                new Coordinate(37.515500, 127.007000),
                new Coordinate(37.516000, 127.004500),
                new Coordinate(37.516500, 127.002000),
                new Coordinate(37.517000, 127.000000),
                new Coordinate(37.517500, 126.998000),
                new Coordinate(37.518000, 126.996500),
                new Coordinate(37.518400, 126.995600)
        ));

        var totalLength = course.length();

        assertThat((int) totalLength.value()).isEqualTo(2573);
    }

    @ParameterizedTest
    @CsvSource({
            "37.517712, 126.995012, 142",
            "37.516678, 126.997065, 46"
    })
    void 특정_좌표에서_코스까지_가장_가까운_거리를_계산할_수_있다(double latitude, double longitude, int expectedDistance) {
        var course = new Course("한강뛰어보자", List.of(
                new Coordinate(37.519760, 126.995477),
                new Coordinate(37.517083, 126.997182),
                new Coordinate(37.519760, 126.995477)
        ));
        var target = new Coordinate(latitude, longitude);

        var distance = course.distanceFrom(target);

        assertThat((int) distance.value()).isEqualTo(expectedDistance);
    }

    @Test
    void 코스_위의_점에서_코스까지의_거리는_0에_가깝다() {
        var course = new Course("직선코스", List.of(
                new Coordinate(37.5, 127.0),
                new Coordinate(37.5, 127.001),
                new Coordinate(37.5, 127.0)
        ));
        var target = new Coordinate(37.5, 127.0005); // 코스 선분의 중점

        var distance = course.distanceFrom(target);

        assertThat(distance.value()).isLessThan(1.0);
    }

    @Test
    void 코스_시작점에서_코스까지의_거리는_0이다() {
        var course = new Course("삼각형코스", List.of(
                new Coordinate(37.5, 127.0),
                new Coordinate(37.501, 127.0),
                new Coordinate(37.5005, 127.001),
                new Coordinate(37.5, 127.0)
        ));
        var target = new Coordinate(37.5, 127.0);

        var distance = course.distanceFrom(target);

        assertThat(distance.value()).isEqualTo(0.0);
    }

    @Test
    void 코스_내부의_점에서_코스까지의_거리를_계산한다() {
        var course = new Course("사각형코스", List.of(
                new Coordinate(37.5, 127.0),
                new Coordinate(37.501, 127.0),
                new Coordinate(37.501, 127.001),
                new Coordinate(37.5, 127.001),
                new Coordinate(37.5, 127.0)
        ));
        var target = new Coordinate(37.5005, 127.0005); // 사각형 중앙

        var distance = course.distanceFrom(target);

        assertThat((int) distance.value()).isEqualTo(44);
    }

    @Test
    void 코스_외부_멀리_떨어진_점에서_코스까지의_거리를_계산한다() {
        var course = new Course("작은원형코스", List.of(
                new Coordinate(37.5, 127.0),
                new Coordinate(37.5001, 127.0),
                new Coordinate(37.5, 127.0001),
                new Coordinate(37.4999, 127.0),
                new Coordinate(37.5, 127.0)
        ));
        var target = new Coordinate(37.52, 127.02); // 매우 멀리 떨어진 점

        var distance = course.distanceFrom(target);

        assertThat((int) distance.value()).isEqualTo(2829);
    }

    @ParameterizedTest
    @CsvSource({
            "37.4999, 126.9999, 14",   // 코스 바로 옆
            "37.5001, 127.0001, 0",    // 코스 반대편 바로 옆 (코스 위의 점)
            "37.5, 126.999, 88"        // 코스에서 서쪽으로 100m
    })
    void 코스_주변_다양한_위치에서의_거리를_계산한다(double latitude, double longitude, int expectedDistance) {
        var course = new Course("정사각형코스", List.of(
                new Coordinate(37.5, 127.0),
                new Coordinate(37.5001, 127.0),
                new Coordinate(37.5001, 127.0001),
                new Coordinate(37.5, 127.0001),
                new Coordinate(37.5, 127.0)
        ));
        var target = new Coordinate(latitude, longitude);

        var distance = course.distanceFrom(target);

        assertThat((int) distance.value()).isEqualTo(expectedDistance);
    }

    @ParameterizedTest
    @CsvSource({
            "-10, -10, 0, 0",
            "20, 20, 10, 10",
            "10, 0, 5, 5",
            "5, 0, 2.5, 2.5"
    })
    void 코스의_좌표_중에서_가장_가까운_좌표를_계산한다(double targetLatitude, double targetLongitude, double latitude, double longitude) {
        Course course = new Course("왕복코스", List.of(
                new Coordinate(0, 0),
                new Coordinate(10, 10.0),
                new Coordinate(0, 0)
        ));
        Coordinate target = new Coordinate(targetLatitude, targetLongitude);

        Coordinate minDistanceCoordinate = course.closestCoordinateFrom(target);

        Coordinate expectedCoordinate = new Coordinate(latitude, longitude);
        assertThat(minDistanceCoordinate).isEqualTo(expectedCoordinate);
    }

    @ParameterizedTest
    @MethodSource("createArguments")
    void 코스의_난이도를_계산한다(List<Coordinate> coordinates, RoadType roadType, double expectedDifficulty) {
        var course = new Course("코스", roadType, coordinates);

        var difficulty = course.difficulty();

        assertThat(difficulty).isEqualTo(expectedDifficulty);
    }

    private static Stream<Arguments> createArguments() {
        return Stream.of(
                Arguments.of(
                        List.of(new Coordinate(37.5, 127.0), new Coordinate(37.5, 127.0), new Coordinate(37.5, 127.0)),
                        RoadType.트랙,
                        1
                ),
                Arguments.of(
                        List.of(new Coordinate(37.5, 127.0), new Coordinate(37.5, 127.0), new Coordinate(37.5, 127.0)),
                        RoadType.트레일,
                        1
                ),
                Arguments.of(
                        List.of(new Coordinate(37.5, 127.0), new Coordinate(37.5, 127.0), new Coordinate(37.5, 127.0)),
                        RoadType.보도,
                        1
                ),
                Arguments.of(
                        List.of(new Coordinate(37.499384, 126.999433), new Coordinate(37.501806, 127.239550), new Coordinate(37.499384, 126.999433)),
                        RoadType.보도,
                        10
                ),
                Arguments.of(
                        List.of(new Coordinate(37.506591, 127.145630), new Coordinate(37.311405, 127.383810), new Coordinate(37.506591, 127.145630)),
                        RoadType.트랙,
                        10
                ),
                Arguments.of(
                        List.of(new Coordinate(37.486225, 127.063228), new Coordinate(37.486557, 127.187908), new Coordinate(37.486225, 127.063228)),
                        RoadType.트레일,
                        10
                )
        );
    }
}

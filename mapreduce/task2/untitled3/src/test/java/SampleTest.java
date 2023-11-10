import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SampleTest {
    @Test
    @DisplayName("Фильтрация четных чисел")
    void filterEvenNumbers() {
        // given
        int[] numbers = new int[]{1, 2, 3, 4, 5};

        // when
        int[] evenNumbers = evenNumbers(numbers);

        // then
        assertThat(evenNumbers)
                .containsExactly(2, 4)
                .hasSize(2);
    }

    @Test
    void failedTest() {
        assertThat(5).isEqualTo(2 * 2);
    }

    int[] evenNumbers(int[] numbers) {
        int count = countEvenNumbers(numbers);

        int[] result = new int[count];
        int idx = 0;
        for (int number : numbers) {
            if (number % 2 == 0) {
                result[idx++] = number;
            }
        }
        return result;
    }

    int countEvenNumbers(int[] numbers) {
        int count = 0;
        for (int number : numbers) {
            if (number % 2 == 0) {
                ++count;
            }
        }
        return count;
    }
}
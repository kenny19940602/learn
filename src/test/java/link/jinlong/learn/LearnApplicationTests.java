package link.jinlong.learn;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LearnApplicationTests {

        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void shouldReturnACashCardWhenDataIsPresent() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards/99", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                DocumentContext documentContext = JsonPath.parse(response.getBody());
                Number id = documentContext.read("$.id");
                assertThat(id).isEqualTo(99);

                Double amount = documentContext.read("$.amount");
                assertThat(amount).isEqualTo(123.45);
        }

        @Test
        void shouldNotReturnACashCardWithAnUnknownId() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards/1000", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(response.getBody()).isBlank();
        }

        @Test
        @DirtiesContext
        void shouldCreateANewCashCard() {
                CashCard newCashCard = new CashCard(null, 250.00, null);
                ResponseEntity<Void> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .postForEntity("/cashcards", newCashCard, Void.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                URI locationOfNewCashCard = response.getHeaders().getLocation();
                ResponseEntity<String> getResponse = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity(locationOfNewCashCard, String.class);
                assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
                Number id = documentContext.read("$.id");
                Double amount = documentContext.read("$.amount");

                assertThat(id).isNotNull();
                assertThat(amount).isEqualTo(250.00);
        }

        @Test
        void shouldReturnAllCashCardsWhenListIsRequested() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                DocumentContext documentContext = JsonPath.parse(response.getBody());
                int cashCardCount = documentContext.read("$.length()");
                assertThat(cashCardCount).isEqualTo(3);

                List<Number> ids = documentContext.read("$..id");
                assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

                List<Double> amounts = documentContext.read("$..amount");
                assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
        }

        @Test
        void shouldReturnAPageOfCashCards() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards?page=0&size=1", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                DocumentContext documentContext = JsonPath.parse(response.getBody());
                JSONArray page = documentContext.read("$[*]");
                assertThat(page.size()).isEqualTo(1);

        }

        @Test
        void shouldReturnASortedPageOfCashCards() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                DocumentContext documentContext = JsonPath.parse(response.getBody());
                JSONArray page = documentContext.read("$[*]");
                assertThat(page.size()).isEqualTo(1);

                Double amount = documentContext.read("$[0].amount");
                assertThat(amount).isEqualTo(150.00);
        }

        @Test
        void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                DocumentContext documentContext = JsonPath.parse(response.getBody());
                JSONArray page = documentContext.read("$[*]");
                assertThat(page.size()).isEqualTo(3);

                JSONArray amounts = documentContext.read("$..amount");
                assertThat(amounts).containsExactlyInAnyOrder(1.00, 123.45, 150.00);
        }

        @Test
        void shouldNotReturnACashCardWhenUsingBadCredentials() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("BAD-USER", "abc123")
                                .getForEntity("/cashcards/99", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

                response = restTemplate
                                .withBasicAuth("sarah1", "BAD-PASSWORD")
                                .getForEntity("/cashcards/99", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void shouldRejectUsersWhoAreNotCardOwners() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("hank-owns-no-cards", "qrs456")
                                .getForEntity("/cashcards/99", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards/102", String.class);// kumar2's card
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DirtiesContext
        void shouldUpdateAnExistingCashCard() {
                CashCard updateCashCard = new CashCard(null, 19.99, null);
                HttpEntity<CashCard> request = new HttpEntity<>(updateCashCard);
                ResponseEntity<Void> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

                ResponseEntity<String> getResponse = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards/99", String.class);
                assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
                Number id = documentContext.read("$.id");
                assertThat(id).isEqualTo(99);

                Double amount = documentContext.read("$.amount");
                assertThat(amount).isEqualTo(19.99);
        }

        @Test
        void shouldNotUpdateACashCardThatDoesNotExist() {
                CashCard unknownCashCard = new CashCard(null, 19.99, null);
                HttpEntity<CashCard> request = new HttpEntity<>(unknownCashCard);
                ResponseEntity<Void> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .exchange("/cashcards/1000", HttpMethod.PUT, request, Void.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
                CashCard kumarsCard = new CashCard(null, 333.33, "kumar2");
                HttpEntity<CashCard> request = new HttpEntity<>(kumarsCard);
                ResponseEntity<Void> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DirtiesContext
        void shouldDeleteAnExistingCashCard() {
                ResponseEntity<Void> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

                ResponseEntity<String> getResponse = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .getForEntity("/cashcards/99", String.class);
                assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldNotDeleteACashCardThatDoesNotExist() {
                ResponseEntity<Void> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .exchange("/cashcards/9999", HttpMethod.DELETE, null, Void.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {
                ResponseEntity<Void> response = restTemplate
                                .withBasicAuth("sarah1", "abc123")
                                .exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                ResponseEntity<Void> getResponse = restTemplate.withBasicAuth("kumar", "xyz789")
                                .getForEntity("/cashcards/102", Void.class);
                assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
}

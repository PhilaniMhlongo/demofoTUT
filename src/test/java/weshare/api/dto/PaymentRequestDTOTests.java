package weshare.api.dto;

import org.junit.jupiter.api.Test;
import weshare.model.Expense;
import weshare.model.PaymentRequest;
import weshare.model.Person;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static weshare.model.DateHelper.DD_MM_YYYY;
import static weshare.model.DateHelper.TOMORROW;
import static weshare.model.MoneyHelper.amountOf;

public class PaymentRequestDTOTests {
    Person p1 = new Person("student1@wethinkcode.co.za");
    Person p2 = new Person("student2@wethinkcode.co.za");
    Expense expense = new Expense(p1, "Lunch", amountOf(100), LocalDate.of(2022, 1, 31));
    PaymentRequest paymentRequest = expense.requestPayment(p2, amountOf(100), TOMORROW);

    @Test
    public void fromPaymentRequest() {
        PaymentRequestDTO dto = PaymentRequestDTO.fromPaymentRequest(paymentRequest);
        assertThat(dto.getId()).isEqualTo(paymentRequest.getId());
        assertThat(dto.getExpenseId()).isEqualTo(expense.getId());
        assertThat(dto.getFromPersonId()).isEqualTo(paymentRequest.getPersonRequestingPayment().getId());
        assertThat(dto.getToPersonId()).isEqualTo(paymentRequest.getPersonWhoShouldPayBack().getId());
        assertThat(dto.getAmount()).isEqualTo(paymentRequest.getAmountToPay().getNumber().longValue());
        assertThat(dto.getDate()).isEqualTo(DD_MM_YYYY.format(paymentRequest.getDueDate()));
        assertThat(dto.isPaid()).isEqualTo(paymentRequest.isPaid());
    }
}

package pl.nethos.rekrutacja.views;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.nethos.rekrutacja.bank_account.BankAccount;
import pl.nethos.rekrutacja.bank_account.BankAccountRepository;
import pl.nethos.rekrutacja.contractor.Contractor;
import pl.nethos.rekrutacja.contractor.ContractorRepository;

@PageTitle("Zadanie")
@Route("/zadanie")
public class ExerciseView extends VerticalLayout {

    public ExerciseView(@Autowired ContractorRepository contractorRepository,
                        @Autowired BankAccountRepository bankAccountRepository) {
        setSpacing(false);
        setSizeFull();
//        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");

        for (Contractor contractor : contractorRepository.all()) {
            add(new Label(contractor.toString()));
        }

        for (BankAccount bankAccount : bankAccountRepository.all()) {
            add(new Label(bankAccount.toString()));
        }
    }
}

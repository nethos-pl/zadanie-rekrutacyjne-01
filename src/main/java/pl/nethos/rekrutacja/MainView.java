package pl.nethos.rekrutacja;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import org.springframework.beans.factory.annotation.Autowired;
import pl.nethos.rekrutacja.kontrahent.Kontrahent;
import pl.nethos.rekrutacja.kontrahent.KontrahentRepository;

@Route
@PWA(name = "Nethos - Zadanie rekrutacyjne na stanowisko programisty", shortName = "Nethos - Rekrutacja")
public class MainView extends VerticalLayout {

    public MainView(@Autowired KontrahentRepository kontrahentRepository) {
        setSizeFull();

        wyswietl(kontrahentRepository);
    }

    private void wyswietl(KontrahentRepository kontrahentRepository) {
        for (Kontrahent kontrahent : kontrahentRepository.all()) {
            add(new Label(kontrahent.toString()));
        }
    }
}

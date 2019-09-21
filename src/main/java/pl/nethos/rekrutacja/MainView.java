package pl.nethos.rekrutacja;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import pl.nethos.rekrutacja.kontrahent.KontrahentRepository;
import pl.nethos.rekrutacja.kontrahent.Kontrahent;

import java.util.concurrent.ThreadLocalRandom;

@Route
@PWA(name = "Nethos - Zadanie rekrutacyjne na stanowisko programisty", shortName = "Nethos - Rekrutacja")
public class MainView extends VerticalLayout {

    @Autowired
    private KontrahentRepository kontrahentRepository;

    public MainView() {
        Button button = new Button("Dodaj kontrahenta", e -> dodajKontrahenta());
        add(button);
    }

    private void dodajKontrahenta() {
        final String nazwa = "Kontrahent #" + ThreadLocalRandom.current().nextInt(100);

        Kontrahent kontrahent = new Kontrahent();
        kontrahent.setNazwa(nazwa);
        kontrahentRepository.save(kontrahent);

        Notification.show(String.format("Dodano %s", nazwa));
    }

}

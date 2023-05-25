package pl.nethos.rekrutacja.views;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.nethos.rekrutacja.konto_bankowe.KontoBankowe;
import pl.nethos.rekrutacja.konto_bankowe.KontoBankoweRepository;
import pl.nethos.rekrutacja.kontrahent.Kontrahent;
import pl.nethos.rekrutacja.kontrahent.KontrahentRepository;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@PageTitle("Ekran główny")
@Route("")
public class KontrahentListView extends Div {

    private KontoBankoweRepository kontoBankoweRepository;

    public KontrahentListView(@Autowired KontrahentRepository kontrahentRepository,
                              @Autowired KontoBankoweRepository kontoBankoweRepository)  {

        this.kontoBankoweRepository = kontoBankoweRepository;

        Grid<Kontrahent> kontrahentGrid = new Grid<>(Kontrahent.class, false);

        // STYLE THE GRID
        // Grid striped.
        kontrahentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Dynamic height.
        kontrahentGrid.setAllRowsVisible(true);

        // Highlight the row.
        kontrahentGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Build the structure.
        kontrahentGrid.addColumn(Kontrahent::getNazwa).setHeader("Kontrahent");
        kontrahentGrid.addColumn(Kontrahent::getNip).setHeader("NIP");

        // Add details to each row by renderer.
        kontrahentGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createItemDetailsGrid));


        // FILL THE GRID WITH DATA
        kontrahentGrid.setItems(kontrahentRepository.all());

        // Adding my grid to UI.
        add(kontrahentGrid);
    }

    private Component createItemDetailsGrid(Kontrahent kontrahent) {
        Grid<KontoBankowe> kontoBankoweGrid = new Grid<>(KontoBankowe.class, false);

        // STYLE THE GRID
        // Grid striped.
        kontoBankoweGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Dynamic height.
        kontoBankoweGrid.setAllRowsVisible(true);

        // Highlight the row.
        kontoBankoweGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        kontoBankoweGrid.addColumn(new ComponentRenderer<>(kontoBankowe -> {
            String numer = kontoBankowe.getNumer();
            String formattedNumer = formatNumer(numer);
            Component component = new Text(formattedNumer);

            return component;
        })).setHeader("Numer konta").setAutoWidth(true);

        kontoBankoweGrid.addColumn(KontoBankowe::getAktywne).setHeader("Aktywne");
        kontoBankoweGrid.addColumn(KontoBankowe::getDomyslne).setHeader("Domyślne");
        kontoBankoweGrid.addColumn(KontoBankowe::getWirtualne).setHeader("Wirtualne");

        kontoBankoweGrid.addColumn(new ComponentRenderer<>(kontoBankowe -> {
            Integer stanWeryfkacji = kontoBankowe.getStanWeryfkacji();
            String theme;
            Button button = new Button("",
                    buttonClickEvent -> {
                        // TODO: handle exceptions properly
                        try {
                            isVerified(kontrahent.getNip(), kontoBankowe.getNumer());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            if (stanWeryfkacji != null) {
                theme = String.format("badge %s", "success");
            } else {
                theme = String.format("badge %s", "error");
            }

            button.getElement().setAttribute("theme", theme);
            button.setText("Nie weryfikowano");

            return button;
        })).setHeader("Stan Weryfikacji");


        // FILL THE GRID WITH DATA
        kontoBankoweGrid.setItems(kontoBankoweRepository.specificKontrahent(kontrahent.getId()));

        return kontoBankoweGrid;
    }

    private String formatNumer(String numer) {
        String formattedNumer = "";

        // We want new string to be in format: xx xxxx xxxx xxxx xxxx xxxx xxxx.
        for (int i = 0; i < numer.length(); i++) {
            // First two digits or all next groups of four.
            if (i == 2 || (i % 4 == 2 && i > 0))
                formattedNumer += " ";

            formattedNumer += numer.charAt(i);
        }

        return formattedNumer;
    }

    // TODO: test exceptions?
    private void isVerified(String nip, String numer) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest testGetRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://wl-test.mf.gov.pl/api/check/nip/" + nip + "/bank-account/" + numer))
                .build();

//        HttpRequest productionGetRequest = HttpRequest.newBuilder()
//                .uri(URI.create("https://wl-api.mf.gov.pl/api/check/nip/" + nip + "/bank-account/" + numer))
//                .build();

        HttpResponse<String> response = client.send(testGetRequest,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }

//    private static final SerializableBiConsumer<Span, KontoBankowe> statusComponentUpdater = (
//            span, kontoBankowe) -> {
//        boolean isAvailable = "Available".equals(kontoBankowe.getStatus());
//        String theme = String.format("badge %s",
//                isAvailable ? "success" : "error");
//        span.getElement().setAttribute("theme", theme);
//        span.setText(kontoBankowe.getStatus());
//    };
//
//    private static ComponentRenderer<Span, Kontrahent> createStatusComponentRenderer() {
//        return new ComponentRenderer<>(Span::new, statusComponentUpdater);
//    }
}

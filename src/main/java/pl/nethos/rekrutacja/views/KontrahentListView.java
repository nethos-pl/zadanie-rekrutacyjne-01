package pl.nethos.rekrutacja.views;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.nethos.rekrutacja.konto_bankowe.KontoBankowe;
import pl.nethos.rekrutacja.konto_bankowe.KontoBankoweRepository;
import pl.nethos.rekrutacja.kontrahent.Kontrahent;
import pl.nethos.rekrutacja.kontrahent.KontrahentRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@PageTitle("Ekran główny")
@Route("")
@CssImport("./styles/MainGridViewStyle.css")
public class KontrahentListView extends Div {
    private static final AtomicInteger NOT_ASSIGNED = new AtomicInteger(0);
    private static final AtomicInteger ASSIGNED = new AtomicInteger(1);
    private static final AtomicInteger STH_WENT_WRONG = new AtomicInteger(2);

    private final KontoBankoweRepository kontoBankoweRepository;

    public KontrahentListView(@Autowired KontrahentRepository kontrahentRepository,
                              @Autowired KontoBankoweRepository kontoBankoweRepository)  {

        this.kontoBankoweRepository = kontoBankoweRepository;

        Grid<Kontrahent> kontrahentGrid = new Grid<>(Kontrahent.class, false);
        kontrahentGrid.setClassName("kontrahent-konto-bankowe-grid");

        // STYLE THE GRID
        // Grid striped.
        kontrahentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Dynamic height.
        kontrahentGrid.setAllRowsVisible(true);

        // Highlight the row.
        kontrahentGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Build the structure.
        Label nazwaHeaderLabel = new Label("Kontrahent");
        nazwaHeaderLabel.addClassName("kontrahent-grid-headers");
        kontrahentGrid.addColumn(Kontrahent::getNazwa).setHeader(nazwaHeaderLabel);


        Label nipHeaderLabel = new Label("NIP");
        nipHeaderLabel.addClassName("kontrahent-grid-headers");
        kontrahentGrid.addColumn(Kontrahent::getNip).setHeader(nipHeaderLabel);


        // Add details to each row by renderer.
        kontrahentGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createItemDetailsGrid));

        kontrahentGrid.setAllRowsVisible(true);

        // FILL THE GRID WITH DATA
        kontrahentGrid.setItems(kontrahentRepository.all());

        // Adding my grid to UI.
        add(kontrahentGrid);
    }

    private Component createItemDetailsGrid(Kontrahent kontrahent) {
        Grid<KontoBankowe> kontoBankoweGrid = new Grid<>(KontoBankowe.class, false);
        kontoBankoweGrid.setClassName("konto-bankowe-grid");


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

            return (Component) new Text(formattedNumer);
        })).setHeader("Numer konta").setAutoWidth(true).setFlexGrow(0);

        kontoBankoweGrid.addColumn(KontoBankowe::getAktywne).setHeader("Aktywne");
        kontoBankoweGrid.addColumn(KontoBankowe::getDomyslne).setHeader("Domyślne");
        kontoBankoweGrid.addColumn(KontoBankowe::getWirtualne).setHeader("Wirtualne");

        kontoBankoweGrid.addColumn(new ComponentRenderer<>(kontoBankowe -> {
            AtomicInteger verificationStatus = STH_WENT_WRONG;
            AtomicReference<Button> verificationButton = new AtomicReference<>(new Button());
            verificationButton.set(new Button("",
                    buttonClickEvent -> {
                        // TODO: handle exceptions properly
                        try {
                            verificationStatus.set(verifyAccount(kontrahent, kontoBankowe, kontoBankoweRepository));
                            updateVerificationButton(verificationButton, verificationStatus, kontoBankowe);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
            ));


            String theme = "badge error";
            if (kontoBankowe.getStanWeryfkacji() == null) {
                verificationButton.get().setText("Nieokreślony");
                theme = String.format("badge %s", "information");
            } else if (kontoBankowe.getStanWeryfkacji() == ASSIGNED.get()){
                theme = String.format("badge %s", "success");
                verificationButton.get().setText("Zweryfikowany");
            } else if (kontoBankowe.getStanWeryfkacji() == NOT_ASSIGNED.get()) {
                verificationButton.get().setText("Błędne konto");
            }

            verificationButton.get().getElement().setAttribute("theme", theme);
            verificationButton.get().setTooltipText(kontoBankowe.getDataWeryfikacji());

            return verificationButton.get();
        })).setHeader("Stan Weryfikacji");

        kontoBankoweGrid.setAllRowsVisible(true);

        // FILL THE GRID WITH DATA
        kontoBankoweGrid.setItems(kontoBankoweRepository.specificKontrahent(kontrahent.getId()));

        return kontoBankoweGrid;
    }

    private String formatNumer(String numer) {
        StringBuilder formattedNumer = new StringBuilder();

        // We want new string to be in format: xx xxxx xxxx xxxx xxxx xxxx xxxx.
        for (int i = 0; i < numer.length(); i++) {
            // First two digits or all next groups of four.
            if (i == 2 || i % 4 == 2)
                formattedNumer.append(" ");

            formattedNumer.append(numer.charAt(i));
        }

        return formattedNumer.toString();
    }

    private int verifyAccount(Kontrahent kontrahent, KontoBankowe kontoBankowe, KontoBankoweRepository kontoBankoweRepository)
            throws IOException, InterruptedException, ParseException {

        HttpClient client = HttpClient.newHttpClient();
        // Request for test API.
//        HttpRequest getRequest = HttpRequest.newBuilder()
//                .uri(URI.create("https://wl-test.mf.gov.pl/api/check/nip/" +
//                        kontrahent.getNip() + "/bank-account/" + kontoBankowe.getNumer()))
//                .build();

        // Request for production API.
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://wl-api.mf.gov.pl/api/check/nip/" +
                        kontrahent.getNip() + "/bank-account/" + kontoBankowe.getNumer()))
                .build();

        HttpResponse<String> response = client.send(getRequest,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);

        String newTimestamp = jsonObject
                        .getAsJsonObject("result")
                        .get("requestDateTime")
                        .getAsString();

        String inputFormat = "dd-MM-yyyy HH:mm:ss";
        String outputFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat);
        SimpleDateFormat outputFormatter = new SimpleDateFormat(outputFormat);
        Date date = inputFormatter.parse(newTimestamp);

        kontoBankowe.setDataWeryfikacji(outputFormatter.format(date));

        String accountAssigned = jsonObject
                .getAsJsonObject("result")
                .get("accountAssigned")
                .getAsString();

        if (accountAssigned.equals("TAK")) {
            kontoBankowe.setStanWeryfkacji(ASSIGNED.get());
            kontoBankoweRepository.merge(kontoBankowe);
            return ASSIGNED.get();
        } else if (accountAssigned.equals("NIE")){
            kontoBankowe.setStanWeryfkacji(NOT_ASSIGNED.get());
            kontoBankoweRepository.merge(kontoBankowe);
            return NOT_ASSIGNED.get();
        } else {
            // Updating only date.
            kontoBankoweRepository.merge(kontoBankowe);
            return STH_WENT_WRONG.get();
        }
    }

    private void updateVerificationButton(AtomicReference<Button> button,
                                          AtomicInteger verificationStatus,
                                          KontoBankowe kontoBankowe) {
        String theme = "badge error";
        if (verificationStatus.get() == ASSIGNED.get()){
            theme = String.format("badge %s", "success");
            button.get().setText("Zweryfikowany");
        } else if (verificationStatus.get() == NOT_ASSIGNED.get()) {
            button.get().setText("Błędne konto");
        }

        button.get().getElement().setAttribute("theme", theme);
        button.get().setTooltipText(kontoBankowe.getDataWeryfikacji());
    }
}

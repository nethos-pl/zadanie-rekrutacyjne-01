package pl.nethos.rekrutacja.views;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
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

@PageTitle("Ekran główny")
@Route("")
@CssImport("./styles/MainGridViewStyle.css")
public class KontrahentListView extends Div {
    private static final AtomicInteger NOT_ASSIGNED = new AtomicInteger(0);
    private static final AtomicInteger ASSIGNED = new AtomicInteger(1);

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

        // For CSS purpose.
        kontoBankoweGrid.setClassName("konto-bankowe-grid");

        // STYLE THE GRID
        // Grid striped.
        kontoBankoweGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Dynamic height.
        kontoBankoweGrid.setAllRowsVisible(true);

        // Highlight the row.
        kontoBankoweGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        kontoBankoweGrid
                .addColumn(new ComponentRenderer<>(this::createFormattedNumer))
                .setHeader("Numer konta").setAutoWidth(true).setFlexGrow(0);

        kontoBankoweGrid.addColumn(KontoBankowe::getAktywne).setHeader("Aktywne");
        kontoBankoweGrid.addColumn(KontoBankowe::getDomyslne).setHeader("Domyślne");
        kontoBankoweGrid.addColumn(KontoBankowe::getWirtualne).setHeader("Wirtualne");

        kontoBankoweGrid
                .addColumn(new ComponentRenderer<>(kontoBankowe -> createVerificationButton(kontrahent, kontoBankowe)))
                .setHeader("Stan Weryfikacji");

        kontoBankoweGrid.setAllRowsVisible(true);

        // FILL THE GRID WITH DATA
        kontoBankoweGrid.setItems(kontoBankoweRepository.specificKontrahent(kontrahent.getId()));

        return kontoBankoweGrid;
    }

    private Component createVerificationButton(Kontrahent kontrahent, KontoBankowe kontoBankowe) {
        AtomicReference<Button> verificationButton = new AtomicReference<>(new Button());
        verificationButton.set(new Button("",
                buttonClickEvent -> {
                    // Verification on demand.
                    verifyAccount(kontrahent, kontoBankowe);
                    updateVerificationButton(verificationButton, kontoBankowe);
                }
        ));

        // Check verification at start.
        updateVerificationButton(verificationButton, kontoBankowe);

        return verificationButton.get();
    }

    private Component createFormattedNumer(KontoBankowe kontoBankowe) {
        String numer = kontoBankowe.getNumer();
        String formattedNumer = formatNumer(numer);

        return (Component) new Text(formattedNumer);
    }

    private void verifyAccount(Kontrahent kontrahent, KontoBankowe kontoBankowe) {
        JsonObject jsonResponse = jsonResponseFromWlGovApi(kontrahent, kontoBankowe);
        if (jsonResponse.has("result")) {
            String newTimestamp = jsonResponse
                    .getAsJsonObject("result")
                    .get("requestDateTime")
                    .getAsString();

            String newDateString = formatDate(newTimestamp);
            if (!newDateString.equals(""))
                kontoBankowe.setDataWeryfikacji(newDateString);
            else
                kontoBankowe.setDataWeryfikacji("Niepoprawny format daty.");

            String accountAssigned = jsonResponse
                    .getAsJsonObject("result")
                    .get("accountAssigned")
                    .getAsString();

            if (accountAssigned.equals("TAK")) {
                kontoBankowe.setStanWeryfkacji(ASSIGNED.get());
                kontoBankoweRepository.merge(kontoBankowe);
            } else if (accountAssigned.equals("NIE")){
                kontoBankowe.setStanWeryfkacji(NOT_ASSIGNED.get());
                kontoBankoweRepository.merge(kontoBankowe);
            } else {
                // Updating only date.
                kontoBankoweRepository.merge(kontoBankowe);
            }
        } else if (jsonResponse.has("code")) {
            String code = jsonResponse.get("code").getAsString();
            String message = jsonResponse.get("message").getAsString();
            dbErrorDialog(code + ": " + message);
        } else {
            dbErrorDialog("Sprawdzenie konta nie powiodło się odpowiedź z serwera jest pusta. " +
                    "Skontaktuj się z autorem programu.");
        }
    }

    private JsonObject jsonResponseFromWlGovApi(Kontrahent kontrahent, KontoBankowe kontoBankowe){
        HttpClient client = HttpClient.newHttpClient();
        // Request for test white list GOV API.
//        HttpRequest getRequest = HttpRequest.newBuilder()
//                .uri(URI.create("https://wl-test.mf.gov.pl/api/check/nip/" +
//                        kontrahent.getNip() + "/bank-account/" + kontoBankowe.getNumer()))
//                .build();

        // Request for production white list GOV API.
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://wl-api.mf.gov.pl/api/check/nip/" +
                        kontrahent.getNip() + "/bank-account/" + kontoBankowe.getNumer()))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // Connection
            dbErrorDialog("Sprawdzenie konta nie powiodło się. Sprawdź swoje połączenie z internetem.");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // Multi-threading
            dbErrorDialog("Sprawdzenie konta nie powiodło się ze względu na zajętość procesora. " +
                    "Spróbuj jeszcze raz.");
            throw new RuntimeException(e);
        }

        System.out.println(response.body());

        JsonObject jsonResponse = null;
        if (!response.equals(null)) {
            Gson gson = new Gson();
            jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        }

        return jsonResponse;
    }

    private String formatDate(String newTimestamp) {
        String inputFormat = "dd-MM-yyyy HH:mm:ss";
        String outputFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat);
        SimpleDateFormat outputFormatter = new SimpleDateFormat(outputFormat);

        Date date = null;
        try {
            date = inputFormatter.parse(newTimestamp);
        } catch (ParseException e) {
            // Parsing went wrong
            dbErrorDialog("Sprawdzenie konta nie powiodło się ze względu na złe formatowanie treści. " +
                    "Skontaktuj się z autorem programu.");
            throw new RuntimeException(e);
        }

        String newDateString = outputFormatter.format(date);

        if (!newDateString.equals(""))
            return newDateString;
        else
            return "";
    }

    private void dbErrorDialog(String errorText){
        Dialog dialog = new Dialog();
        Label infoLabel = new Label(errorText + "\n");
        dialog.add(infoLabel);
        dialog.open();
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

    private void updateVerificationButton(AtomicReference<Button> verificationButton, KontoBankowe kontoBankowe) {
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
    }
}

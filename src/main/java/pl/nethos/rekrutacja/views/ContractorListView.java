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
import pl.nethos.rekrutacja.bank_account.BankAccount;
import pl.nethos.rekrutacja.bank_account.BankAccountRepository;
import pl.nethos.rekrutacja.contractor.Contractor;
import pl.nethos.rekrutacja.contractor.ContractorRepository;

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
public class ContractorListView extends Div {
    private static final AtomicInteger NOT_ASSIGNED = new AtomicInteger(0);
    private static final AtomicInteger ASSIGNED = new AtomicInteger(1);

    private final BankAccountRepository bankAccountRepository;

    public ContractorListView(@Autowired ContractorRepository contractorRepository,
                              @Autowired BankAccountRepository bankAccountRepository)  {

        this.bankAccountRepository = bankAccountRepository;

        Grid<Contractor> contractorGrid = new Grid<>(Contractor.class, false);
        contractorGrid.setClassName("contractor-bank-account-grid");

        // STYLE THE GRID
        // Grid striped.
        contractorGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Dynamic height.
        contractorGrid.setAllRowsVisible(true);

        // Highlight the row.
        contractorGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Build the structure.
        Label nazwaHeaderLabel = new Label("Kontrahent");
        nazwaHeaderLabel.addClassName("contractor-grid-headers");
        contractorGrid.addColumn(Contractor::getName).setHeader(nazwaHeaderLabel);

        Label nipHeaderLabel = new Label("NIP");
        nipHeaderLabel.addClassName("contractor-grid-headers");
        contractorGrid.addColumn(Contractor::getNip).setHeader(nipHeaderLabel);

        // Add details to each row by renderer.
        contractorGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createItemDetailsGrid));

        contractorGrid.setAllRowsVisible(true);

        // FILL THE GRID WITH DATA
        contractorGrid.setItems(contractorRepository.all());

        // Adding my grid to UI.
        add(contractorGrid);
    }

    private Component createItemDetailsGrid(Contractor contractor) {
        Grid<BankAccount> bankAccountGrid = new Grid<>(BankAccount.class, false);

        // For CSS purpose.
        bankAccountGrid.setClassName("bank-account-grid");

        // STYLE THE GRID
        // Grid striped.
        bankAccountGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Dynamic height.
        bankAccountGrid.setAllRowsVisible(true);

        // Highlight the row.
        bankAccountGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        bankAccountGrid
                .addColumn(new ComponentRenderer<>(this::createFormattedNumer))
                .setHeader("Numer konta").setAutoWidth(true).setFlexGrow(0);

        bankAccountGrid.addColumn(BankAccount::getActive).setHeader("Aktywne");
        bankAccountGrid.addColumn(BankAccount::getDefaultAccount).setHeader("Domyślne");
        bankAccountGrid.addColumn(BankAccount::getVirtual).setHeader("Wirtualne");

        bankAccountGrid
                .addColumn(new ComponentRenderer<>(bankAccount -> createVerificationButton(contractor, bankAccount)))
                .setHeader("Stan Weryfikacji");

        bankAccountGrid.setAllRowsVisible(true);

        // FILL THE GRID WITH DATA
        bankAccountGrid.setItems(bankAccountRepository.specificKontrahent(contractor.getId()));

        return bankAccountGrid;
    }

    private Component createVerificationButton(Contractor contractor, BankAccount bankAccount) {
        AtomicReference<Button> verificationButton = new AtomicReference<>(new Button());
        verificationButton.set(new Button("",
                buttonClickEvent -> {
                    // Verification on demand.
                    verifyAccount(contractor, bankAccount);
                    updateVerificationButton(verificationButton, bankAccount);
                }
        ));

        // Check verification at start.
        updateVerificationButton(verificationButton, bankAccount);

        return verificationButton.get();
    }

    private Component createFormattedNumer(BankAccount bankAccount) {
        String numer = bankAccount.getNumber();
        String formattedNumer = formatNumer(numer);

        return new Text(formattedNumer);
    }

    private void verifyAccount(Contractor contractor, BankAccount bankAccount) {
        JsonObject jsonResponse = jsonResponseFromWlGovApi(contractor, bankAccount);
        if (jsonResponse.has("result")) {
            String newTimestamp = jsonResponse
                    .getAsJsonObject("result")
                    .get("requestDateTime")
                    .getAsString();

            String newDateString = formatDate(newTimestamp);
            if (!newDateString.equals(""))
                bankAccount.setVerificationDate(newDateString);
            else
                bankAccount.setVerificationDate("Niepoprawny format daty.");

            String accountAssigned = jsonResponse
                    .getAsJsonObject("result")
                    .get("accountAssigned")
                    .getAsString();

            if (accountAssigned.equals("TAK")) {
                bankAccount.setVerificationStatus(ASSIGNED.get());
                bankAccountRepository.merge(bankAccount);
            } else if (accountAssigned.equals("NIE")){
                bankAccount.setVerificationStatus(NOT_ASSIGNED.get());
                bankAccountRepository.merge(bankAccount);
            } else {
                // Updating only date.
                bankAccountRepository.merge(bankAccount);
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

    private JsonObject jsonResponseFromWlGovApi(Contractor contractor, BankAccount bankAccount){
        HttpClient client = HttpClient.newHttpClient();
        // Request for test white list GOV API.
//        HttpRequest getRequest = HttpRequest.newBuilder()
//                .uri(URI.create("https://wl-test.mf.gov.pl/api/check/nip/" +
//                        contractor.getNip() + "/bank-account/" + bankAccount.getNumber()))
//                .build();

        // Request for production white list GOV API.
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://wl-api.mf.gov.pl/api/check/nip/" +
                        contractor.getNip() + "/bank-account/" + bankAccount.getNumber()))
                .build();

        HttpResponse<String> response;
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

//        System.out.println(response.body());

        JsonObject jsonResponse = null;
        if (response != null) {
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

        Date date;
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

    private void updateVerificationButton(AtomicReference<Button> verificationButton, BankAccount bankAccount) {
        String theme = "badge error";
        if (bankAccount.getVerificationStatus() == null) {
            verificationButton.get().setText("Nieokreślony");
            theme = String.format("badge %s", "information");
        } else if (bankAccount.getVerificationStatus() == ASSIGNED.get()){
            theme = String.format("badge %s", "success");
            verificationButton.get().setText("Zweryfikowany");
        } else if (bankAccount.getVerificationStatus() == NOT_ASSIGNED.get()) {
            verificationButton.get().setText("Błędne konto");
        }

        verificationButton.get().getElement().setAttribute("theme", theme);
        verificationButton.get().setTooltipText(bankAccount.getVerificationDate());
    }
}

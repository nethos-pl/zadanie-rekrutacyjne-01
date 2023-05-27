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

/**
 * Advanced view showing contractors and related bank accounts.
 * There is an option to check if the bank account is valid on White List in Poland using government API.
 */
@PageTitle("Ekran główny")
@Route("")
@CssImport("./styles/MainGridViewStyle.css")
public class ContractorListView extends Div {
    private static final int NOT_ASSIGNED = 0;
    private static final int ASSIGNED = 1;

    private final BankAccountRepository bankAccountRepository;

    /**
     * Constructor that sets up main grid with contractors.
     * Also, it calls the bank account grid to be made as item details for every row.
     *
     * @param contractorRepository Instance of our contractor repository as dependency injection.
     * @param bankAccountRepository Instance of our bank account repository as dependency injection.
     */
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

    /**
     * Builds an item details component.
     * It will be used to build up item details grid by calling it for every contractor instance.
     *
     * @param contractor Instance of the contractor that we want details from.
     * @return Component instance that can be used to style item details.
     */
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
                .addColumn(new ComponentRenderer<>(this::createFormattedNumber))
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

    /**
     * Builds a verification button to manage verification status.
     *
     * @param contractor Instance of the contractor that the bank account relate.
     * @param bankAccount Instance of the bank account that we assign button with.
     * @return Component instance that can be used to add to item details.
     */
    private Component createVerificationButton(Contractor contractor, BankAccount bankAccount) {
        // Atomic because we have to ensure atomicity and thread-safety when updating the button.
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

    /**
     * Create component with formatted number that will be used in the item details.
     *
     * @param bankAccount Instance of the bank account that we assign button with.
     * @return Component instance that can be used to add to item details.
     */
    private Component createFormattedNumber(BankAccount bankAccount) {
        String number = bankAccount.getNumber();
        String formattedNumber = formatNumber(number);

        return new Text(formattedNumber);
    }

    /**
     * Verify if the account is assigned to the contractor on government White List.
     * It also generates dialogs for user if something won't go as planned.
     *
     * @param bankAccount Instance of the bank account that we want to check.
     * @param contractor Instance of the contractor that the bank account should relate.
     */
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
                bankAccount.setVerificationStatus(ASSIGNED);
                bankAccountRepository.merge(bankAccount);
            } else if (accountAssigned.equals("NIE")){
                bankAccount.setVerificationStatus(NOT_ASSIGNED);
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

    /**
     * Makes http call to government White List API.
     * Also, it shows dialog for user if something went wrong.
     *
     * @param contractor Instance of the contractor that the bank account relate.
     * @param bankAccount Instance of the bank account that we are checking.
     * @return JsonObject with the server response.
     */
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

    /**
     * Format date from White List API response to our database format.
     *
     * @param newTimestamp Timestamp that will be formatted.
     * @return String with formatted date.
     */
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

    /**
     * Creates simple error dialog with specific information to user.
     * It has no other elements than text.
     *
     * @param errorText Information to user.
     */
    private void dbErrorDialog(String errorText){
        Dialog dialog = new Dialog();
        Label infoLabel = new Label(errorText + "\n");
        dialog.add(infoLabel);
        dialog.open();
    }

    /**
     * Format the account number
     * to: XX XXXX XXXX XXXX XXXX XXXX XXXX
     * from: XXXXXXXXXXXXXXXXXXXXXXXXXX.
     *
     * @param number Number to format.
     * @return String with formatted account number.
     */
    private String formatNumber(String number) {
        StringBuilder formattedNumber = new StringBuilder();

        // We want new string to be in format: xx xxxx xxxx xxxx xxxx xxxx xxxx.
        for (int i = 0; i < number.length(); i++) {
            // First two digits or all next groups of four.
            if (i == 2 || i % 4 == 2)
                formattedNumber.append(" ");

            formattedNumber.append(number.charAt(i));
        }

        return formattedNumber.toString();
    }

    /**
     * Update the appearance of verification button in item details.
     *
     * @param verificationButton Verification button.
     * @param bankAccount Instance of the bank account from which we get verification status.
     */
    private void updateVerificationButton(AtomicReference<Button> verificationButton, BankAccount bankAccount) {
        String theme = "badge error";
        if (bankAccount.getVerificationStatus() == null) {
            verificationButton.get().setText("Nieokreślony");
            theme = String.format("badge %s", "information");
        } else if (bankAccount.getVerificationStatus() == ASSIGNED){
            theme = String.format("badge %s", "success");
            verificationButton.get().setText("Zweryfikowany");
        } else if (bankAccount.getVerificationStatus() == NOT_ASSIGNED) {
            verificationButton.get().setText("Błędne konto");
        }

        verificationButton.get().getElement().setAttribute("theme", theme);
        verificationButton.get().setTooltipText(bankAccount.getVerificationDate());
    }
}

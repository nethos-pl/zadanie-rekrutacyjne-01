package pl.nethos.rekrutacja.bank_account;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "KONTO_BANKOWE")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_account_gen")
    @SequenceGenerator(name = "bank_account_gen", sequenceName = "bank_account_seq", allocationSize = 1)
    @Getter
    private long id;

    @Getter
    @Column(name = "ID_KONTRAHENT")
    private long idContractor;

    @Getter
    @Column(name = "NUMER")
    private String number;

    @Getter
    @Column(name = "AKTYWNE")
    private Integer active;

    @Getter
    @Column(name = "DOMYSLNE")
    private Integer defaultAccount;

    @Getter
    @Column(name = "WIRTUALNE")
    private Integer virtual;

    @Getter
    @Setter
    @Column(name = "STAN_WERYFIKACJI")
    private Integer verificationStatus;

    @Getter
    @Setter
    @Column(name = "DATA_WERYFIKACJI")
    private String verificationDate;

    @Override
    public String toString() {
        return "BankAccount{" +
                "id=" + id +
                ", idContractor='" + idContractor + '\'' +
                ", number='" + number + '\'' +
                ", active='" + active + '\'' +
                ", defaultAccount='" + defaultAccount + '\'' +
                ", virtual='" + virtual + '\'' +
                ", verificationStatus='" + verificationStatus + '\'' +
                ", verificationDate='" + verificationDate + '\'' +
                '}';
    }
}

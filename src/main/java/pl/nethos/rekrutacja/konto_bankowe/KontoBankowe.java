package pl.nethos.rekrutacja.konto_bankowe;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
public class KontoBankowe {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "konto_bankowe_gen")
    @SequenceGenerator(name = "konto_bankowe_gen", sequenceName = "konto_bankowe_seq", allocationSize = 1)
    @Getter
    private long id;

    @Getter
    private long idKontrahent;

    @Getter
    private String numer;

    @Getter
    private Integer aktywne;

    @Getter
    private Integer domyslne;

    @Getter
    private Integer wirtualne;

    @Getter
    @Column(name = "STAN_WERYFIKACJI")
    private Integer stanWeryfkacji;

    @Getter
    private Timestamp dataWeryfikacji;

    @Override
    public String toString() {
        return "KontoBankowe{" +
                "id=" + id +
                ", idKontrahent='" + idKontrahent + '\'' +
                ", numer='" + numer + '\'' +
                ", aktywne='" + aktywne + '\'' +
                ", domyslne='" + domyslne + '\'' +
                ", wirtualne='" + wirtualne + '\'' +
                ", stanWeryfikacji='" + stanWeryfkacji + '\'' +
                ", dataWeryfikacji='" + dataWeryfikacji + '\'' +
                '}';
    }
}

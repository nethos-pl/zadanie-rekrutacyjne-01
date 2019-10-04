package pl.nethos.rekrutacja.kontrahent;

import javax.persistence.*;

@Entity
public class Kontrahent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kontrahent_gen")
    @SequenceGenerator(name="kontrahent_gen", sequenceName = "kontrahent_seq", allocationSize = 1)
    private long id;

    private String nazwa;

    private String nip;

    public long getId() {
        return id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    @Override
    public String toString() {
        return "Kontrahent{" +
                "id=" + id +
                ", nazwa='" + nazwa + '\'' +
                ", nip='" + nip + '\'' +
                '}';
    }
}

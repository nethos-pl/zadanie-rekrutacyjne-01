package pl.nethos.rekrutacja.kontrahent;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Kontrahent {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kontrahent_gen")
	@SequenceGenerator(name = "kontrahent_gen", sequenceName = "kontrahent_seq", allocationSize = 1)
	@Getter
	private long id;

	@Getter
	@Setter
	private String nazwa;

	@Getter
	@Setter
	private String nip;

	@Override
	public String toString() {
		return "Kontrahent{" +
				"id=" + id +
				", nazwa='" + nazwa + '\'' +
				", nip='" + nip + '\'' +
				'}';
	}
}

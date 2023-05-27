package pl.nethos.rekrutacja.contractor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that represent contractor in delivered database.
 */
@Entity
@Table(name = "KONTRAHENT")
public class Contractor {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contractor_gen")
	@SequenceGenerator(name = "contractor_gen", sequenceName = "contractor_seq", allocationSize = 1)
	@Getter
	private long id;

	@Getter
	@Setter
	@Column(name = "NAZWA")
	private String name;

	@Getter
	@Setter
	private String nip;

	/**
	 * Prints Contractor instance with its every member.
	 */
	@Override
	public String toString() {
		return "Contractor{" +
				"id=" + id +
				", name='" + name + '\'' +
				", nip='" + nip + '\'' +
				'}';
	}
}

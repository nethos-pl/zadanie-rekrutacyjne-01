package pl.nethos.rekrutacja.contractor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Class that manages the contractors.
 * It is responsible for setting up connection to database and single unit data exchange.
 */
@Service
@Transactional
public class ContractorRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Send query for all Contractors.
     *
     * @return The list of all contractors stored in database.
     */
    public List<Contractor> all() {
        return em.createQuery("SELECT k FROM Contractor k", Contractor.class).getResultList();
    }
}

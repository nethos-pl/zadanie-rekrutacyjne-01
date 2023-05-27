package pl.nethos.rekrutacja.contractor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContractorRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Contractor> all() {
        return em.createQuery("SELECT k FROM Contractor k", Contractor.class).getResultList();
    }
}

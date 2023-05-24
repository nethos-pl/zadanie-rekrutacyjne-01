package pl.nethos.rekrutacja.kontrahent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class KontrahentRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Kontrahent> all() {
        return em.createQuery("SELECT k FROM Kontrahent k", Kontrahent.class).getResultList();
    }
}

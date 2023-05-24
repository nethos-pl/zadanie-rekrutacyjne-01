package pl.nethos.rekrutacja.konto_bankowe;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class KontoBankoweRepository {

    @PersistenceContext
    private EntityManager em;

    public List<KontoBankowe> all() {
        return em.createQuery("SELECT b FROM KontoBankowe b", KontoBankowe.class).getResultList();
    }
}

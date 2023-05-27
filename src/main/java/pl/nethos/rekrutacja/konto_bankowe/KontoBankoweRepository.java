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

    public List<KontoBankowe> specificKontrahent(long idKontrahent) {
        return em.createQuery("SELECT b FROM KontoBankowe b WHERE idKontrahent="+idKontrahent, KontoBankowe.class).getResultList();
    }

    public void merge(KontoBankowe kontoBankowe) {
        em.merge(kontoBankowe);
    }
}

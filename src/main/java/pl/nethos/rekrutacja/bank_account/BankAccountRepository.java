package pl.nethos.rekrutacja.bank_account;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BankAccountRepository {

    @PersistenceContext
    private EntityManager em;

    public List<BankAccount> all() {
        return em.createQuery("SELECT b FROM BankAccount b", BankAccount.class).getResultList();
    }

    public List<BankAccount> specificKontrahent(long idContractor) {
        return em.createQuery("SELECT b FROM BankAccount b WHERE idContractor=" + idContractor, BankAccount.class).getResultList();
    }

    public void merge(BankAccount bankAccount) {
        em.merge(bankAccount);
    }
}

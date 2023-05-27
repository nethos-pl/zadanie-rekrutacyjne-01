package pl.nethos.rekrutacja.bank_account;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Class that manages the bank accounts.
 * It is responsible for setting up connection to database and single unit data exchange.
 */
@Service
@Transactional
public class BankAccountRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Send query for all BankAccounts.
     *
     * @return The list of all bank accounts stored in database.
     */
    public List<BankAccount> all() {
        return em.createQuery("SELECT b FROM BankAccount b", BankAccount.class).getResultList();
    }

    /**
     * Send query for all BankAccounts.
     *
     * @param idContractor Id of the bank account contractor.
     * @return The list of all bank accounts stored in database.
     */
    public List<BankAccount> specificKontrahent(long idContractor) {
        return em.createQuery("SELECT b FROM BankAccount b WHERE idContractor=" + idContractor, BankAccount.class).getResultList();
    }

    /**
     * Update database with our instance.
     *
     * @param bankAccount Bank account instance to be updated in database.
     */
    public void merge(BankAccount bankAccount) {
        em.merge(bankAccount);
    }
}

package tk.puncha.dao.hibernate;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tk.puncha.dao.VisitDAO;
import tk.puncha.models.Visit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("hibernate")
@Component
public class HibernateVisitDAO implements VisitDAO {

  @PersistenceContext
  private EntityManager em;

  @Override
  public void deleteVisit(int visitId) {
    em.remove(em.getReference(Visit.class, visitId));
  }

  @Override
  public int insertVisit(Visit visit) {
    visit = em.merge(visit);
    return visit.getId();
  }

}
package gumga.framework.application;

import gumga.framework.domain.repository.GumgaCrudAndQueryNotOnlyTypedRepository;
import gumga.framework.domain.repository.GumgaCrudRepository;
import gumga.framework.domain.repository.GumgaQueryDSLRepository;
import java.io.Serializable;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class GumgaRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable> extends JpaRepositoryFactoryBean<R, T, I> {

	@SuppressWarnings("rawtypes")
	protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
		return new MyRepositoryFactory(entityManager);
	}

	private static class MyRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {
		private final EntityManager entityManager;

		public MyRepositoryFactory(EntityManager entityManager) {
			super(entityManager);
			this.entityManager = entityManager;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected Object getTargetRepository(RepositoryMetadata metadata) {
			Class<?> repositoryInterface = metadata.getRepositoryInterface();
			Class<?> domainClass = metadata.getDomainType();
			                 
			if (isQueryDslExecutor(repositoryInterface)) {
				return new GumgaQueryDSLRepositoryImpl<>((JpaEntityInformation<?, Serializable>) JpaEntityInformationSupport.getMetadata(domainClass, entityManager), entityManager);
                        }else if(isQueryNoTyped(repositoryInterface)){                     
                                return new GumgaCrudAndQueryNotOnlyTypedRepositoryImpl<>((JpaEntityInformation<?, Serializable>) JpaEntityInformationSupport.getMetadata(domainClass, entityManager), entityManager);
                        }
                       
                        return new GumgaGenericRepository(JpaEntityInformationSupport.getMetadata(domainClass, entityManager), entityManager);
		}

		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
			if (isQueryDslExecutor(metadata.getRepositoryInterface())) {
				return GumgaQueryDSLRepository.class;
                        }else if(isQueryNoTyped(metadata.getRepositoryInterface())){
                                return GumgaCrudAndQueryNotOnlyTypedRepository.class;
                        }
			return GumgaCrudRepository.class;
		}
	}

	private static boolean isQueryDslExecutor(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}
        
        private static boolean isQueryNoTyped(Class<?> repositoryInterface){

            
            return GumgaCrudAndQueryNotOnlyTypedRepository.class.isAssignableFrom(repositoryInterface);

       
        }
	
}

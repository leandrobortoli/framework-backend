package gumga.framework.presentation.gateway;

import gumga.framework.application.service.GumgaNoDeleteService;
import gumga.framework.core.GumgaIdable;
import gumga.framework.core.QueryObject;
import gumga.framework.core.SearchResult;
import gumga.framework.core.utils.ReflectionUtils;
import gumga.framework.domain.service.GumgaReadableServiceable;
import gumga.framework.domain.service.GumgaWritableServiceable;
import gumga.framework.presentation.GumgaTranslator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class GumgaNoDeleteGateway<A extends GumgaIdable<?>, DTO> implements GumgaReadableServiceable<DTO>, GumgaWritableServiceable<DTO> {

	@Autowired
	private GumgaNoDeleteService<A, ?> delegate;
	
	@Autowired
	private GumgaTranslator<A, DTO> translator;
	
	@Override
	public SearchResult<DTO> pesquisa(QueryObject query) {
		SearchResult<A> pesquisa = delegate.pesquisa(query);
		return new SearchResult<>(query, pesquisa.getCount(), translator.from((List<A>) pesquisa.getValues()));
	}

	@Override
	public DTO view(Long id) {
		return translator.from(delegate.view(id));
	}

	@Override
	public DTO save(DTO resource) {
		return translator.from(delegate.save(translator.to(resource)));
	}
	
	@SuppressWarnings("unchecked")
	public Class<DTO> clazz() {
		return (Class<DTO>) ReflectionUtils.inferGenericType(getClass());
	}

}

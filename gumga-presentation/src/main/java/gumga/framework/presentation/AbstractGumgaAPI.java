package gumga.framework.presentation;

import gumga.framework.core.QueryObject;
import gumga.framework.core.SearchResult;
import gumga.framework.domain.GumgaServiceable;
import gumga.framework.presentation.validation.Error;
import gumga.framework.presentation.validation.ErrorResource;
import gumga.framework.presentation.validation.FieldErrorResource;
import gumga.framework.validation.exception.InvalidEntityException;

import java.lang.reflect.Constructor;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class AbstractGumgaAPI<T> {
	
	@Autowired
    private Validator validator;
	
	protected GumgaServiceable<T> service;
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	public AbstractGumgaAPI(GumgaServiceable<T> service) {
		this.service = service;
	}
	
	@RequestMapping
	public SearchResult<T> pesquisa(QueryObject query) {
		SearchResult<T> pesquisa = service.pesquisa(query);
		return new SearchResult<>(query, pesquisa.getCount(), pesquisa.getValues());
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST)
	public RestResponse<T> save(@RequestBody @Valid T model, BindingResult result) {
		T entity = saveOrCry(model, result);
		return new RestResponse<T>(entity, getEntitySavedMessage(entity));
	}
	
	@RequestMapping("/{id}")
	public T load(@PathVariable Long id) {
		return service.view(id);
	}
	
	@Transactional
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
	public RestResponse<T> update(@PathVariable("id") Long id, @Valid @RequestBody T model, BindingResult result) {
		T entity = saveOrCry(model, result);
		return new RestResponse<T>(entity, getEntityUpdateMessage(entity));
	}
	
	@Transactional
	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public RestResponse<T> delete(@PathVariable Long id, HttpServletRequest request) {
		T entity = service.view(id);
		service.delete(entity);
		
		return new RestResponse<T>(getEntityDeletedMessage(entity));
	}

	@RequestMapping("/new")
	public T initialState() {
		return initialValue();
	}
	
	protected T initialValue() {
		try {
			Constructor<T> constructor = service.clazz().getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private T saveOrCry(T model, BindingResult result) {
		if (result.hasErrors())
			throw new InvalidEntityException(result);

		return service.save(model);
	}
	
	protected String getEntityName(T entity) {
		return entity.getClass().getSimpleName();
	}
	
	protected String getEntitySavedMessage(T entity) {
		return getEntityName(entity) + " saved successfully";
	}
	
	protected String getEntityUpdateMessage(T entity) {
		return getEntitySavedMessage(entity);
	}
	
	protected String getEntityDeletedMessage(T entity) {
		return getEntityName(entity) + " deleted successfully";
	}
	
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	public ErrorResource validate(@RequestBody T entity) {
		try {
			Set<ConstraintViolation<T>> errors = validator.validate(entity);
			if (errors.isEmpty()) return ErrorResource.NO_ERRORS;
			
			ErrorResource invalidEntity = new ErrorResource(Error.INVALID_ENTITY, "Invalid Entity");
			invalidEntity.setData(entity);
			invalidEntity.setDetails("Invalid Entity State");
			
			for (ConstraintViolation<T> violation : errors) {
				FieldErrorResource fieldErrorResource = new FieldErrorResource();
				fieldErrorResource.setResource(violation.getRootBeanClass().getCanonicalName());
				fieldErrorResource.setField(violation.getPropertyPath().toString());
				fieldErrorResource.setCode(violation.getMessageTemplate());
				fieldErrorResource.setMessage(violation.getMessage());
				
				invalidEntity.addFieldError(fieldErrorResource);
			}
			
			return invalidEntity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

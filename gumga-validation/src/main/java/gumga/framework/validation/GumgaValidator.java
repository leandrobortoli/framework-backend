package gumga.framework.validation;

import gumga.framework.validation.exception.InvalidEntityException;
import gumga.framework.validation.validator.GumgaCommonValidator;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import com.google.common.base.Optional;

/**
 * Classe que realiza a validação em uma lista de {@link GumgaFieldValidator}
 * 
 */
public class GumgaValidator {

	private Errors errors;

	public GumgaValidator(Errors errors) {
		this.errors = errors;
	}

	public GumgaValidator(Object object) {
		this(object, object.getClass().getSimpleName().toLowerCase());
	}

	public GumgaValidator(Object object, String name) {
		this(new BeanPropertyBindingResult(object, name));
	}

	@SafeVarargs
	public final <T> GumgaValidator check(String property, T value,
			GumgaFieldValidator<? super T>... validators) {
		for (GumgaFieldValidator<? super T> validator : validators) {
			Optional<GumgaValidationError> errorCode = validator.validate(
					value, this.errors);
			if (errorCode.isPresent()) {
				GumgaValidationError error = errorCode.get();
				this.errors.rejectValue(property, error.getCode(),
						error.getArgs(), null);
				break;
			}
		}
		return this;
	}

	public final GumgaValidator checkIsTrue(String property, Boolean value) {
		return check(property, value, GumgaCommonValidator.isTrue());
	}

	public final GumgaValidator checkIsFalse(String property, Boolean value) {
		return check(property, value, GumgaCommonValidator.isFalse());
	}

	public final GumgaValidator checkNotNull(String property, Object value) {
		return check(property, value, GumgaCommonValidator.notNull());
	}

	public static final GumgaValidator with(Errors errors) {
		return new GumgaValidator(errors);
	}

	public void check() {
		if (this.errors.hasErrors()) {
			throw new InvalidEntityException(this.errors);
		}
	}

}

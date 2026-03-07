export type RegistrationRole = 'INDIVIDUAL' | 'BUSINESS';

export type RegistrationFormValues = {
  email: string;
  password: string;
  confirmPassword: string;
  fullName: string;
  contactNumber: string;
  businessName: string;
  businessAddress: string;
  permitNumber: string;
  role: RegistrationRole;
};

export type RegistrationFieldErrors = Partial<Record<keyof RegistrationFormValues, string>>;

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const contactNumberRegex = /^\+?[0-9]{7,15}$/;

export function validateEmail(email: string): string | null {
  if (!email.trim()) {
    return 'Email is required.';
  }

  if (!emailRegex.test(email.trim())) {
    return 'Please enter a valid email format.';
  }

  return null;
}

export function validatePassword(password: string): string | null {
  if (!password) {
    return 'Password is required.';
  }

  if (password.length < 8) {
    return 'Password must be at least 8 characters.';
  }

  return null;
}

export function validateRegistrationForm(values: RegistrationFormValues): RegistrationFieldErrors {
  const errors: RegistrationFieldErrors = {};

  const emailError = validateEmail(values.email);
  const passwordError = validatePassword(values.password);

  if (emailError) {
    errors.email = emailError;
  }

  if (passwordError) {
    errors.password = passwordError;
  }

  if (!values.confirmPassword) {
    errors.confirmPassword = 'Confirm password is required.';
  } else if (values.confirmPassword !== values.password) {
    errors.confirmPassword = 'Passwords do not match.';
  }

  if (values.role === 'BUSINESS') {
    if (!values.businessName.trim()) {
      errors.businessName = 'Business name is required.';
    }

    if (!values.businessAddress.trim()) {
      errors.businessAddress = 'Business address is required.';
    }

    if (!values.permitNumber.trim()) {
      errors.permitNumber = 'Permit number is required.';
    }
  } else {
    if (!values.fullName.trim()) {
      errors.fullName = 'Full name is required.';
    } else if (values.fullName.trim().length < 2) {
      errors.fullName = 'Full name must be at least 2 characters.';
    }
  }

  if (!values.contactNumber.trim()) {
    errors.contactNumber = 'Contact number is required.';
  } else if (!contactNumberRegex.test(values.contactNumber.trim())) {
    errors.contactNumber = 'Contact number must be numeric and may include a leading + country code.';
  }

  return errors;
}

export function hasValidationErrors(errors: RegistrationFieldErrors): boolean {
  return Object.values(errors).some(Boolean);
}

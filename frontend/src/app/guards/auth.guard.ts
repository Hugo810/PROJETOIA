import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLogado()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

export const roleGuard = (...roles: string[]): CanActivateFn => {
  return (route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isLogado()) {
      router.navigate(['/login']);
      return false;
    }

    if (auth.temRole(...roles)) {
      return true;
    }

    router.navigate(['/']);
    return false;
  };
};

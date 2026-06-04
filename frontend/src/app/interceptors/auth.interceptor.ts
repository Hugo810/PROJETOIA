import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const data = localStorage.getItem('taskflow_user');
  if (data) {
    try {
      const user = JSON.parse(data);
      if (user && user.id) {
        const cloned = req.clone({
          setHeaders: { 'X-User-Id': user.id.toString() }
        });
        return next(cloned);
      }
    } catch {
    }
  }
  return next(req);
};

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
  refreshToken: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface ApiError {
  message: string;
  status: number;
  code?: string;
}

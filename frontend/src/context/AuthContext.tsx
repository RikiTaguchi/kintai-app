"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { apiClient } from "@/lib/apiClient";

interface BaseUser {
  id: string;
  loginId: string;
  classroomId: string;
  classroomName: string;
  classroomNumber: number;
  firstName: string;
  lastName: string;
}

export interface ManagerUser extends BaseUser {
  role: "ROLE_MANAGER";
}

export interface TutorUser extends BaseUser {
  role: "ROLE_TUTOR";
  tutorNumber: number;
  terminated: boolean;
  terminationDate: string | null;
}

export type AuthUser = ManagerUser | TutorUser;

interface AuthContextType {
  user: AuthUser | null;
  login: (userData: Omit<ManagerUser, "role"> | Omit<TutorUser, "role">, role: "ROLE_MANAGER" | "ROLE_TUTOR") => void;
  logout: () => Promise<void>;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem("user_info");
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem("user_info");
      }
    }
    setIsLoading(false);
  }, []);

  const login = (
    userData: Omit<ManagerUser, "role"> | Omit<TutorUser, "role">,
    role: "ROLE_MANAGER" | "ROLE_TUTOR"
  ) => {
    const fullUserData = { ...userData, role } as AuthUser;
    setUser(fullUserData);
    localStorage.setItem("user_info", JSON.stringify(fullUserData));
  };

  const logout = async () => {
    const isManager = user?.role === 'ROLE_MANAGER';
    const logoutUrl = isManager ? '/api/managers/logout' : '/api/tutors/logout';

    try {
        await apiClient(logoutUrl, { method: 'POST' });
    } catch {
        // API失敗時もローカル状態はクリアしてリダイレクト
    } finally {
        setUser(null);
        localStorage.removeItem('user_info');
        window.location.href = isManager ? '/manager/login' : '/tutor/login';
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}

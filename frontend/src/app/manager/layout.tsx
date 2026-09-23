"use client";

import { usePathname } from "next/navigation";
import ManagerLayout from "@/components/layout/ManagerLayout";
import { ReactNode } from "react";

const PUBLIC_PATHS = ["/manager/login", "/manager/register"];

export default function Layout({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  if (PUBLIC_PATHS.includes(pathname)) return <>{children}</>;
  return <ManagerLayout>{children}</ManagerLayout>;
}

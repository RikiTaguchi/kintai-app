"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function TutorHomePage() {
  const router = useRouter();
  useEffect(() => { router.replace("/tutor/works"); }, [router]);
  return null;
}

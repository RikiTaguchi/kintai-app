"use client";

import { createContext, useContext, useRef, useMemo, useEffect, ReactNode } from "react";
import { useSwipe, SwipeHandlers } from "@/hooks/useSwipe";

interface SwipeContextValue {
  register: (h: SwipeHandlers) => void;
  unregister: () => void;
  mainHandlers: SwipeHandlers;
}

const SwipeContext = createContext<SwipeContextValue>({
  register: () => {},
  unregister: () => {},
  mainHandlers: { onTouchStart: () => {}, onTouchEnd: () => {} },
});

export function SwipeProvider({ children }: { children: ReactNode }) {
  const activeHandlers = useRef<SwipeHandlers | null>(null);

  const mainHandlers = useMemo<SwipeHandlers>(() => ({
    onTouchStart: (e) => activeHandlers.current?.onTouchStart(e),
    onTouchEnd: (e) => activeHandlers.current?.onTouchEnd(e),
  }), []);

  const value = useMemo(() => ({
    register: (h: SwipeHandlers) => { activeHandlers.current = h; },
    unregister: () => { activeHandlers.current = null; },
    mainHandlers,
  }), [mainHandlers]);

  return <SwipeContext.Provider value={value}>{children}</SwipeContext.Provider>;
}

export function useSwipeMainHandlers(): SwipeHandlers {
  return useContext(SwipeContext).mainHandlers;
}

export function useRegisterSwipe(onPrev: () => void, onNext: () => void) {
  const { register, unregister } = useContext(SwipeContext);
  const handlers = useSwipe(onPrev, onNext);

  useEffect(() => {
    register(handlers);
    return () => unregister();
  }, [register, unregister, handlers]);
}

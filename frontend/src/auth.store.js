import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/**
 * Global auth state — survives page refresh via localStorage.
 *
 * After login the backend returns:
 * { accessToken, userId, userName, email, role, labId, labName, labSlug }
 *
 * We split this into two sub-objects:
 *  user  → who is logged in (id, name, email, role)
 *  lab   → which lab they belong to (id, name, slug)
 */
export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      user:  null,
      lab:   null,

      setAuth: (data) => set({
        token: data.accessToken,
        user: {
          id:    data.userId,
          name:  data.userName,
          email: data.email,
          role:  data.role,
        },
        lab: {
          id:   data.labId,
          name: data.labName,
          slug: data.labSlug,
        }
      }),

      clearAuth: () => set({ token: null, user: null, lab: null }),

      // Helper: check if current user has a given role
      isRole: (role) => {
        const state = useAuthStore.getState()
        return state.user?.role === role
      }
    }),
    {
      name: 'pathlab-auth',           // key in localStorage
      partialize: (s) => ({           // only persist these fields
        token: s.token,
        user:  s.user,
        lab:   s.lab
      })
    }
  )
)
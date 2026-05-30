import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import App from './app'
import './index.css'

// React Query client — handles caching, background refetching, error states
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 2,  // data is fresh for 2 minutes
      retry: 1,                   // retry failed requests once
    }
  }
})

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
      {/* Global toast notifications — shown anywhere with toast.success() */}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3500,
          style: { fontSize: '13px' }
        }}
      />
    </QueryClientProvider>
  </React.StrictMode>
)
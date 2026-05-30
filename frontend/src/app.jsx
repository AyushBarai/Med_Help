import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import PrivateRoute from './PrivateRoute'
import Layout from './Layout'
import Login from './Login'
import Dashboard from './Dashboard'
import PatientList from './PatientList'
import RegisterPatient from './RegisterPatient'
import OrderList from './OrderList'
import CreateOrder from './CreateOrder'
import OrderDetail from './OrderDetail'
import ResultEntry from './ResultEntry'
import TestCatalog from './TestCatalog'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<Login />} />

        {/* Protected — all wrapped in Layout (sidebar + header) */}
        <Route element={<PrivateRoute />}>
          <Route element={<Layout />}>
            <Route path="/"               element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard"      element={<Dashboard />} />

            <Route path="/patients"       element={<PatientList />} />
            <Route path="/patients/new"   element={<RegisterPatient />} />

            <Route path="/orders"         element={<OrderList />} />
            <Route path="/orders/new"     element={<CreateOrder />} />
            <Route path="/orders/:id"     element={<OrderDetail />} />
            <Route path="/orders/:id/results" element={<ResultEntry />} />

            <Route path="/catalog"        element={<TestCatalog />} />
          </Route>
        </Route>

        {/* Catch-all */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
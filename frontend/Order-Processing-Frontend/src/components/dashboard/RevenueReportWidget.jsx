import React, { useState, useEffect } from 'react';
import { getDailyRevenueSummary, downloadDailyRevenueCsv, triggerNightlyRevenueJob } from '../../api/orders';
import { StatusBadge } from '../common/StatusBadge';
import { formatCurrency } from '../orders/OrderItemsTable';
import { extractApiError } from '../../api/errorUtils';

export const RevenueReportWidget = () => {
  const todayStr = new Date().toISOString().slice(0, 10);
  const yesterdayStr = new Date(Date.now() - 86400000).toISOString().slice(0, 10);

  const [selectedDate, setSelectedDate] = useState(todayStr);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [triggering, setTriggering] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [jobNotice, setJobNotice] = useState(null);

  const fetchReport = async (date) => {
    try {
      setLoading(true);
      setError('');
      const data = await getDailyRevenueSummary(date);
      setReport(data);
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to load revenue summary report.');
      setError(parsed.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport(selectedDate);
  }, [selectedDate]);

  const handleDownloadCsv = async () => {
    try {
      setDownloading(true);
      await downloadDailyRevenueCsv(selectedDate);
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to download daily revenue CSV.');
      alert(parsed.message);
    } finally {
      setDownloading(false);
    }
  };

  const handleTriggerJob = async () => {
    try {
      setTriggering(true);
      setJobNotice(null);
      const res = await triggerNightlyRevenueJob(selectedDate);
      setJobNotice(res);
      // Refresh data
      await fetchReport(selectedDate);
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to execute nightly revenue export job.');
      alert(parsed.message);
    } finally {
      setTriggering(false);
    }
  };

  return (
    <div className="card" style={{ marginTop: 'var(--space-6)', padding: 'var(--space-5)' }}>
      {/* Header */}
      <div style={{
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
        alignItems: 'center',
        gap: 'var(--space-4)',
        paddingBottom: 'var(--space-4)',
        borderBottom: '1px solid var(--border)'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
              📊 Daily Revenue Report & Nightly Job
            </h2>
            <span style={{
              fontSize: '11px',
              fontWeight: 600,
              color: '#059669',
              background: '#ECFDF5',
              border: '1px solid #A7F3D0',
              padding: '2px 8px',
              borderRadius: '100px',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px'
            }}>
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#10B981' }}></span>
              Scheduled Cron Active (00:00:00)
            </span>
          </div>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0 0' }}>
            Automated nightly revenue summary grouped by order status with CSV file generation
          </p>
        </div>

        {/* Date Selector & Action Buttons */}
        <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 'var(--space-2)' }}>
          <div style={{ display: 'flex', alignItems: 'center', background: 'var(--surface)', borderRadius: '6px', padding: '2px' }}>
            <button
              type="button"
              className={`btn btn-sm ${selectedDate === todayStr ? 'btn-primary' : 'btn-ghost'}`}
              style={{ fontSize: '12px', padding: '4px 10px' }}
              onClick={() => setSelectedDate(todayStr)}
            >
              Today
            </button>
            <button
              type="button"
              className={`btn btn-sm ${selectedDate === yesterdayStr ? 'btn-primary' : 'btn-ghost'}`}
              style={{ fontSize: '12px', padding: '4px 10px' }}
              onClick={() => setSelectedDate(yesterdayStr)}
            >
              Yesterday
            </button>
          </div>

          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="form-input"
            style={{ width: '145px', height: '32px', fontSize: '12px', padding: '0 8px' }}
          />

          <button
            type="button"
            className="btn btn-outline btn-sm"
            onClick={handleDownloadCsv}
            disabled={downloading || loading}
            title="Download CSV report for selected date"
            style={{ fontSize: '12px' }}
          >
            {downloading ? 'Downloading...' : '📥 Export Revenue CSV'}
          </button>

          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={handleTriggerJob}
            disabled={triggering || loading}
            title="Trigger scheduled job immediately to save report to server"
            style={{ fontSize: '12px', background: 'linear-gradient(135deg, #4F46E5 0%, #3730A3 100%)' }}
          >
            {triggering ? 'Running Job...' : '⚡ Run Nightly Job Now'}
          </button>
        </div>
      </div>

      {/* Success Notice when Nightly Job is triggered */}
      {jobNotice && (
        <div style={{
          marginTop: 'var(--space-4)',
          padding: '12px 16px',
          background: '#EFF6FF',
          border: '1px solid #BFDBFE',
          borderLeft: '4px solid #2563EB',
          borderRadius: '8px',
          fontSize: '13px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div>
            <strong>✅ {jobNotice.message}</strong>
            <div style={{ fontSize: '12px', color: '#1E40AF', marginTop: '2px' }}>
              Saved file: <code style={{ background: '#DBEAFE', padding: '2px 6px', borderRadius: '4px' }}>{jobNotice.filePath}</code>
              &nbsp;|&nbsp; Orders: {jobNotice.totalOrders} &nbsp;|&nbsp; Revenue: {formatCurrency(jobNotice.totalRevenue)}
            </div>
          </div>
          <button
            type="button"
            onClick={() => setJobNotice(null)}
            style={{ background: 'transparent', border: 'none', cursor: 'pointer', fontSize: '16px', color: '#64748B' }}
          >
            ✕
          </button>
        </div>
      )}

      {/* Error Notice */}
      {error && (
        <div style={{
          marginTop: 'var(--space-4)',
          padding: '12px 16px',
          background: '#FEF2F2',
          border: '1px solid #FCA5A5',
          borderLeft: '4px solid #DC2626',
          borderRadius: '8px',
          fontSize: '13px',
          color: '#991B1B'
        }}>
          ⚠️ {error}
        </div>
      )}

      {/* Report Table */}
      <div style={{ marginTop: 'var(--space-4)', overflowX: 'auto' }}>
        <table className="table" style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
          <thead>
            <tr style={{ background: 'var(--surface)', borderBottom: '1px solid var(--border)' }}>
              <th style={{ padding: '10px 14px', textAlign: 'left', fontWeight: 600 }}>Order Status</th>
              <th style={{ padding: '10px 14px', textAlign: 'center', fontWeight: 600 }}>Order Count</th>
              <th style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600 }}>Subtotal (INR)</th>
              <th style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600 }}>Tax / GST (18%)</th>
              <th style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 600 }}>Total Revenue (INR)</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', padding: '24px', color: 'var(--text-secondary)' }}>
                  Loading revenue summary for {selectedDate}...
                </td>
              </tr>
            ) : report && report.breakdown && report.breakdown.length > 0 ? (
              <>
                {report.breakdown.map((item) => (
                  <tr key={item.status} style={{ borderBottom: '1px solid var(--border)' }}>
                    <td style={{ padding: '10px 14px' }}>
                      <StatusBadge status={item.status} />
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'center', fontWeight: 600 }}>
                      {item.orderCount}
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'right', fontFamily: 'monospace' }}>
                      {formatCurrency(item.totalSubtotal)}
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'right', fontFamily: 'monospace', color: 'var(--text-secondary)' }}>
                      {formatCurrency(item.totalTax)}
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'right', fontFamily: 'monospace', fontWeight: 700, color: item.totalRevenue > 0 ? '#059669' : 'inherit' }}>
                      {formatCurrency(item.totalRevenue)}
                    </td>
                  </tr>
                ))}
                {/* Total Summary Row */}
                <tr style={{
                  background: 'var(--surface)',
                  fontWeight: 700,
                  borderTop: '2px solid var(--border)',
                  fontSize: '14px'
                }}>
                  <td style={{ padding: '12px 14px', color: 'var(--text-primary)' }}>
                    TOTAL / ALL STATUSES
                  </td>
                  <td style={{ padding: '12px 14px', textAlign: 'center', color: 'var(--brand)' }}>
                    {report.totalOrders}
                  </td>
                  <td style={{ padding: '12px 14px', textAlign: 'right', fontFamily: 'monospace' }}>
                    {formatCurrency(report.totalSubtotal)}
                  </td>
                  <td style={{ padding: '12px 14px', textAlign: 'right', fontFamily: 'monospace' }}>
                    {formatCurrency(report.totalTax)}
                  </td>
                  <td style={{ padding: '12px 14px', textAlign: 'right', fontFamily: 'monospace', color: '#059669' }}>
                    {formatCurrency(report.totalRevenue)}
                  </td>
                </tr>
              </>
            ) : (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)' }}>
                  No orders found for {selectedDate}.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default RevenueReportWidget;

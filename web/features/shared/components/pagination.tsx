import { useState, useEffect } from "react";

type PaginationProps = {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  itemsPerPage?: number;
  totalItems?: number; // Optional, if you want to show "Showing X to Y of Z"
};

export function Pagination({ currentPage, totalPages, onPageChange, itemsPerPage = 8, totalItems }: PaginationProps) {
  const [pageInput, setPageInput] = useState(currentPage.toString());

  useEffect(() => {
    setPageInput(currentPage.toString());
  }, [currentPage]);

  // If there's only 1 page or 0 items, we might still want to render the bar, but let's just not render it if totalPages <= 1 and we have no totalItems
  if (totalPages <= 1 && !totalItems) return null;

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between p-4 sm:px-6 border-t border-slate-700/60 bg-slate-900/50 gap-4 sm:gap-0">
      <span className="text-sm text-slate-400 text-center sm:text-left">
        {totalItems !== undefined ? (
           `Showing ${totalItems === 0 ? 0 : (currentPage - 1) * itemsPerPage + 1} to ${Math.min(currentPage * itemsPerPage, totalItems)} of ${totalItems}`
        ) : (
           `Page ${currentPage} of ${totalPages}`
        )}
      </span>
      <div className="flex items-center gap-2">
        <button 
          disabled={currentPage <= 1} 
          onClick={() => {
            onPageChange(currentPage - 1);
          }}
          className="px-2 py-1 text-sm font-semibold text-slate-300 disabled:opacity-50 hover:text-white transition-colors"
        >
          Prev
        </button>
        <input 
          type="text" 
          value={pageInput}
          onChange={(e) => setPageInput(e.target.value)}
          onBlur={() => {
            let p = parseInt(pageInput);
            if (isNaN(p) || p < 1) p = 1;
            if (p > totalPages) p = totalPages;
            onPageChange(p);
            setPageInput(p.toString());
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              e.currentTarget.blur();
            }
          }}
          className="w-12 rounded border border-slate-700 bg-slate-950/80 px-2 py-1 text-center text-sm text-slate-200 outline-none focus:border-brand"
        />
        <span className="text-sm text-slate-400">of {totalPages}</span>
        <button 
          disabled={currentPage >= totalPages} 
          onClick={() => {
            onPageChange(currentPage + 1);
          }}
          className="px-2 py-1 text-sm font-semibold text-slate-300 disabled:opacity-50 hover:text-white transition-colors"
        >
          Next
        </button>
      </div>
    </div>
  );
}

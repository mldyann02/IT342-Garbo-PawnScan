"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import UserDashboardHeader from "@/components/user-dashboard-header";
import { getAuthUser, getJwt } from "@/lib/auth";
import { fetchReports, updateReport } from "@/lib/reports";

type ReportForm = {
	serialNumber: string;
	itemModel: string;
	description: string;
	file: File | null;
};

export default function EditReportPage() {
	const router = useRouter();
	const params = useParams<{ id: string }>();
	const [form, setForm] = useState<ReportForm>({
		serialNumber: "",
		itemModel: "",
		description: "",
		file: null,
	});
	const [isLoading, setIsLoading] = useState(true);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [submitAttempted, setSubmitAttempted] = useState(false);
	const [feedback, setFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);

	const reportId = Number(params.id);

	useEffect(() => {
		const token = getJwt();
		const authenticatedEmail = getAuthUser();

		if (!token && !authenticatedEmail) {
			router.replace("/login");
			return;
		}

		if (!Number.isFinite(reportId)) {
			setFeedback({ type: "error", message: "Invalid report ID" });
			setIsLoading(false);
			return;
		}

		async function loadReport() {
			setIsLoading(true);
			try {
				const reports = await fetchReports();
				const report = reports.find((entry) => entry.id === reportId);

				if (!report) {
					setFeedback({ type: "error", message: "Report not found" });
					return;
				}

				setForm({
					serialNumber: report.serialNumber,
					itemModel: report.itemModel,
					description: report.description,
					file: null,
				});
			} catch (error) {
				setFeedback({
					type: "error",
					message: error instanceof Error ? error.message : "Failed to load report",
				});
			} finally {
				setIsLoading(false);
			}
		}

		loadReport();
	}, [reportId, router]);

	const errors = useMemo(() => {
		const next: Partial<Record<keyof Omit<ReportForm, "file">, string>> = {};
		if (!form.serialNumber.trim()) {
			next.serialNumber = "Serial number is required";
		}
		if (!form.itemModel.trim()) {
			next.itemModel = "Item model is required";
		}
		if (!form.description.trim()) {
			next.description = "Description is required";
		}
		return next;
	}, [form]);

	const canSubmit = Object.keys(errors).length === 0 && !isSubmitting;

	async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();
		setSubmitAttempted(true);
		setFeedback(null);

		if (Object.keys(errors).length > 0) {
			return;
		}

		setIsSubmitting(true);
		try {
			await updateReport(reportId, {
				serialNumber: form.serialNumber,
				itemModel: form.itemModel,
				description: form.description,
				file: form.file,
			});

			setFeedback({ type: "success", message: "Report updated successfully. Redirecting to My Reports..." });
			setTimeout(() => router.push("/reports?updated=1"), 500);
		} catch (error) {
			setFeedback({
				type: "error",
				message: error instanceof Error ? error.message : "Failed to update report",
			});
		} finally {
			setIsSubmitting(false);
		}
	}

	return (
		<div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
			<UserDashboardHeader />
			<main className="mx-auto w-full max-w-4xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
				<section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
					<p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand/90">Edit Report</p>
					<h1 className="mt-2 text-2xl font-bold text-white sm:text-3xl">Update stolen item report</h1>

					{isLoading ? (
						<div className="mt-6 rounded-lg bg-slate-900/55 p-4 text-sm text-slate-300">Loading report...</div>
					) : (
						<form className="mt-6 grid grid-cols-1 gap-4" onSubmit={handleSubmit} noValidate>
							<label className="flex flex-col gap-2 text-sm">
								<span className="text-slate-300">Serial Number</span>
								<input
									type="text"
									value={form.serialNumber}
									onChange={(event) => setForm((current) => ({ ...current, serialNumber: event.target.value }))}
									className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] focus:outline-none focus:ring-2 focus:ring-brand/50"
								/>
								{submitAttempted && errors.serialNumber && <p className="text-sm text-status-stolen">{errors.serialNumber}</p>}
							</label>

							<label className="flex flex-col gap-2 text-sm">
								<span className="text-slate-300">Item Model</span>
								<input
									type="text"
									value={form.itemModel}
									onChange={(event) => setForm((current) => ({ ...current, itemModel: event.target.value }))}
									className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] focus:outline-none focus:ring-2 focus:ring-brand/50"
								/>
								{submitAttempted && errors.itemModel && <p className="text-sm text-status-stolen">{errors.itemModel}</p>}
							</label>

							<label className="flex flex-col gap-2 text-sm">
								<span className="text-slate-300">Description</span>
								<textarea
									rows={5}
									value={form.description}
									onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
									className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] focus:outline-none focus:ring-2 focus:ring-brand/50"
								/>
								{submitAttempted && errors.description && <p className="text-sm text-status-stolen">{errors.description}</p>}
							</label>

							<label className="flex flex-col gap-2 text-sm">
								<span className="text-slate-300">Replace or Add Attachment (optional)</span>
								<input
									type="file"
									accept="image/*,.pdf"
									onChange={(event) => setForm((current) => ({ ...current, file: event.target.files?.[0] ?? null }))}
									className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-200 file:mr-3 file:rounded-full file:border-0 file:bg-brand file:px-4 file:py-2 file:text-sm file:font-semibold file:text-bg-main"
								/>
							</label>

							{feedback && (
								<div className={`rounded-lg border px-4 py-3 text-sm ${feedback.type === "error" ? "border-status-stolen/40 bg-status-stolen/10 text-status-stolen" : "border-status-clean/40 bg-status-clean/10 text-status-clean"}`}>
									{feedback.message}
								</div>
							)}

							<div className="mt-2 flex flex-wrap gap-3">
								<button type="submit" disabled={!canSubmit} className="w-full rounded-full bg-brand px-5 py-2.5 text-sm font-semibold text-bg-main transition hover:brightness-90 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto">
									{isSubmitting ? "Saving..." : "Save Changes"}
								</button>
								<Link href="/reports" className="w-full rounded-full bg-slate-800/65 px-5 py-2.5 text-center text-sm font-semibold text-slate-200 transition hover:bg-slate-700/75 sm:w-auto">
									Cancel
								</Link>
							</div>
						</form>
					)}
				</section>
			</main>
		</div>
	);
}

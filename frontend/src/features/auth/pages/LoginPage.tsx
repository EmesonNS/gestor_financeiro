export function LoginPage() {
  return (
    <section className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-6">
      <h1 className="text-xl font-semibold tracking-normal">Entrar</h1>
      <form className="mt-6 space-y-4">
        <label className="block text-sm font-medium text-slate-700">
          E-mail
          <input className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2" type="email" />
        </label>
        <label className="block text-sm font-medium text-slate-700">
          Senha
          <input className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2" type="password" />
        </label>
        <button className="w-full rounded-md bg-slate-950 px-4 py-2 font-medium text-white" type="button">
          Acessar
        </button>
      </form>
    </section>
  );
}

import { Ticket } from 'lucide-react'

const photo = (id: string) => `https://images.unsplash.com/photo-${id}?w=360&h=360&fit=crop&auto=format`

// Os mesmos cupons criados pelo seed do order-service (V2__create_coupons.sql).
const banners = [
  {
    code: 'BEMVINDO',
    title: 'R$ 15 OFF',
    subtitle: 'em pedidos a partir de R$ 40',
    className: 'from-brand-500 to-brand-700',
    image: photo('1571091718767-18b5b1457add'),
  },
  {
    code: 'SAVE10',
    title: '10% OFF',
    subtitle: 'acima de R$ 50, até R$ 20 de desconto',
    className: 'from-accent-coral to-brand-500',
    image: photo('1580822184713-fc5400e7fe10'),
  },
  {
    code: 'PIZZA20',
    title: '20% na pizza',
    subtitle: 'no Forno da Nonna, até R$ 30',
    className: 'from-accent-plum to-brand-600',
    image: photo('1513104890138-7c749659a591'),
  },
]

/** Faixa de promoções da home: cada banner mostra um cupom válido para usar na sacola. */
export function PromoBanners() {
  return (
    <div className="no-scrollbar -mx-4 flex snap-x gap-4 overflow-x-auto px-4 pb-1">
      {banners.map((b) => (
        <article
          key={b.code}
          className={`relative flex h-40 min-w-[280px] flex-1 snap-start overflow-hidden rounded-2xl bg-gradient-to-br ${b.className} text-white`}
        >
          <div className="z-10 flex w-[60%] flex-col justify-center gap-1 p-5 pr-0">
            <p className="text-2xl font-black leading-none">{b.title}</p>
            <p className="text-sm leading-snug text-white/90">{b.subtitle}</p>
            <p className="mt-2 inline-flex w-fit items-center whitespace-nowrap gap-1 rounded-full bg-white/20 px-3 py-1 text-xs font-bold backdrop-blur">
              <Ticket size={14} /> Cupom {b.code}
            </p>
          </div>
          <img
            src={b.image}
            alt=""
            loading="lazy"
            className="absolute -right-6 top-1/2 h-28 w-28 -translate-y-1/2 sm:h-36 sm:w-36 rounded-full object-cover shadow-xl ring-4 ring-white/30"
          />
        </article>
      ))}
    </div>
  )
}

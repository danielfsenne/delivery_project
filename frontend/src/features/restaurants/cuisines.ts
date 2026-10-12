const photo = (id: string) => `https://images.unsplash.com/photo-${id}?w=200&h=200&fit=crop&auto=format`

/**
 * Atalhos de culinária da home. O nome precisa ser igual ao campo `cuisine` do restaurante:
 * o filtro do catálogo compara o texto inteiro, sem diferenciar maiúsculas.
 */
export const cuisines = [
  { name: 'Pizza', image: photo('1574071318508-1cdbab80d002') },
  { name: 'Hambúrguer', image: photo('1571091718767-18b5b1457add') },
  { name: 'Japonesa', image: photo('1580822184713-fc5400e7fe10') },
  { name: 'Italiana', image: photo('1621996346565-e3dbc646d9a9') },
  { name: 'Churrasco', image: photo('1599487488170-d11ec9c172f0') },
  { name: 'Saudável', image: photo('1546069901-ba9599a7e63c') },
  { name: 'Mexicana', image: photo('1565299585323-38d6b0865b47') },
  { name: 'Frango', image: photo('1626082927389-6cd097cdc6ec') },
  { name: 'Doces', image: photo('1551024601-bec78aea704b') },
  { name: 'Sorvetes', image: photo('1497034825429-c343d7c6a68f') },
  { name: 'Café', image: photo('1509042239860-f550ce710b93') },
  { name: 'Árabe', image: photo('1529006557810-274b9b2fc783') },
  { name: 'Indiana', image: photo('1585937421612-70a008356fbe') },
  { name: 'Tailandesa', image: photo('1617093727343-374698b1b08d') },
] as const
